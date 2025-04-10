package cn.org.alan.exam.utils;

import cn.org.alan.exam.constant.Constants;
import cn.org.alan.exam.constant.enums.ExecuteTypeEnum;
import cn.org.alan.exam.constant.enums.JudgeTypeEnum;
import cn.org.alan.exam.exception.BusinessException;
import cn.org.alan.exam.pojo.Result;
import cn.org.alan.exam.pojo.dto.JudgeConstraint;
import cn.org.alan.exam.pojo.lang.LangContext;
import cn.org.alan.exam.pojo.vo.ExecuteInfo;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DockerUtil {

    public static Result executeCodeWithDocker(JudgeConstraint judgeConstraint, LangContext langContext, String folderPath) {
        // 创建 DockerClient 用于后续操作
        // 配置远程 Docker 客户端
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.49.130:2375") // 替换为 CentOS 的 IP 地址
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        String containerId = null;
        try {
            // 拉取镜像
//             pullImages(dockerClient, langContext.getImage());
            // 创建并启动容器
            containerId = createAndStartContainer(dockerClient, langContext.getImage(), judgeConstraint.getMemoryLimit(), folderPath);
            // 编译代码
            if(langContext.getCompileCmd() != null) {
                Result compileResult = compile(dockerClient, langContext.getCompileCmd(), containerId, langContext.getSourceFileName());
                if (compileResult != null) {
                    return compileResult;
                }
            }
            // 执行代码
            // 仅执行代码
            if(JudgeTypeEnum.EXECUTE_CODE_ONLY.getState().equals(judgeConstraint.getType())) {
                return executeCodeOnly(dockerClient, langContext.getRunCmd(), containerId, judgeConstraint, folderPath);
            }
            // 普通判题
            else if(JudgeTypeEnum.NORMAL_JUDGE.getState().equals(judgeConstraint.getType())) {
                normalJudge();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExecuteInfo executeInfo = new ExecuteInfo();
            executeInfo.setExecuteType(ExecuteTypeEnum.COMPILE_ERROR.getDesc());
            executeInfo.setExecuteDetail(e.getMessage());
            return Result.success(200, "执行失败", executeInfo);
        } finally {
            // 释放资源（容器）
            if (containerId != null) {
                deleteContainer(dockerClient,containerId);
            }
            // 释放 DockerClient
            try {
                dockerClient.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return Result.error(500, "未知错误");
    }

    /**
     * 拉取镜像
     */
    private static void pullImages(DockerClient dockerClient, String image) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        try {
            pullImageCmd
                    .exec(new PullImageResultCallback())        // 执行回调
                    .awaitCompletion();                         // 阻塞等待执行结束
        } catch (InterruptedException e) {
            System.out.println("拉取" + image + "镜像异常");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建并启动容器
     */
    private static String createAndStartContainer(DockerClient dockerClient, String image, Long memoryLimit, String folderPath) {
        // 配置设置
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(memoryLimit);                     // 限制内存
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.setBinds(new Bind(folderPath, new Volume(Constants.containerVolumePath)));                       // 挂载数据卷
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)                                                          // 禁止网络连接
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        String containerId = createContainerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        return containerId;
    }


    /**
     * 删除容器
     */
    private static void deleteContainer(DockerClient dockerClient, String containerId) {
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
    }

    /**
     * 创建执行命令
     */
    private static ExecCreateCmdResponse createCmdResponse(DockerClient dockerClient, String containerId, String[] cmdArray) {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdArray)
                .withAttachStdin(true)          // 附加到stdin
                .withAttachStderr(true)         // 获取stdout
                .withAttachStdout(true)         // 获取stderr
                .exec();
        return execCreateCmdResponse;
    }


    /**
     * 编译代码
     */
    private static Result compile(DockerClient dockerClient, String langCompileCmd, String containerId, String fileName) {
        String[] cmdArray = String.format(langCompileCmd, Constants.containerVolumePath + "/" + fileName).split(" ");
        // 创建执行命令
        ExecCreateCmdResponse execCreateCmdResponse = createCmdResponse(dockerClient,containerId,cmdArray);
        // 获取输出（编译错误）
        final StringBuilder compileError = new StringBuilder();
        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)) {
                    String errorMessage = new String(frame.getPayload());
                    compileError.append(errorMessage);
                }
                super.onNext(frame);
            }
        };
        try {
            // 执行命令
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(execStartResultCallback)
                    .awaitCompletion(Constants.compileTimeLimit, TimeUnit.MILLISECONDS);
            
            if (compileError.length() > 0) {
                ExecuteInfo executeInfo = new ExecuteInfo();
                executeInfo.setExecuteType(ExecuteTypeEnum.COMPILE_ERROR.getDesc());
                String errorDetail = compileError.toString().trim();
                executeInfo.setExecuteDetail(errorDetail.substring(0, Math.min(errorDetail.length(), Constants.maxCompileErrorLength)));
                return Result.success(200, "编译失败", executeInfo);
            }
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行代码程序
     */
    private static Long[] executeCode(DockerClient dockerClient, String[] runCmd, String containerId, Long timeLimit, File in, OutputStream stdout, OutputStream stderr)
    {
        // 执行命令
        ExecCreateCmdResponse execCreateCmdResponse = createCmdResponse(dockerClient,containerId,runCmd);
        ExecStartResultCallback resultCallback = new ExecStartResultCallback(stdout, stderr);
        // 获取占用的内存
        final Long[] memory = {0L};
        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        statsCmd.exec(new ResultCallback<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {memory[0] = Math.max(memory[0],statistics.getMemoryStats().getUsage());}
            @Override
            public void close() throws IOException {}
            @Override
            public void onStart(Closeable closeable) {}
            @Override
            public void onError(Throwable throwable) {}
            @Override
            public void onComplete() {}
        });

        try(InputStream inputStream = new FileInputStream(in)) {
            long startTime = System.nanoTime();
            // 执行命令
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .withStdIn(inputStream)
                    .exec(resultCallback)
                    .awaitCompletion(timeLimit + Constants.tleTimeLimit, TimeUnit.MILLISECONDS);         // 限制执行时间
            long endTime = System.nanoTime();
            return new Long[] {(endTime - startTime) / 1_000_000, memory[0]};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 仅执行代码
     */
    private static Result executeCodeOnly(DockerClient dockerClient, String[] runCmd, String containerId, JudgeConstraint judgeConstraint, String folderPath) {
        // 获取执行次数
        int num = Math.min(judgeConstraint.getInputs().length, Constants.maxInputsLength);
        List<String> outList = new ArrayList<>();
        List<String> errList = new ArrayList<>();
        long totalTime = 0L;
        long totalMemory = 0L;
        for(int i=0;i<num;i++) {
            File in = new File(folderPath + "/" + i + ".in");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            Long[] execResult = executeCode(dockerClient, runCmd, containerId, judgeConstraint.getTimeLimit(), in, stdout, stderr);
            totalTime += execResult[0];
            totalMemory += execResult[1];
            try {
                String stdoutStr = stdout.toString("UTF-8");
                String stderrStr = stderr.toString("UTF-8");
                outList.add(stdoutStr.substring(0, Math.min(stdoutStr.length(), Constants.maxStdoutLength)));
                errList.add(stderrStr.substring(0, Math.min(stderrStr.length(), Constants.maxStderrLength)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ExecuteInfo executeInfo = new ExecuteInfo();
        executeInfo.setExecuteType(ExecuteTypeEnum.EXECUTE_SUCCESS.getDesc());
        executeInfo.setExecuteDetail("代码执行成功");
        executeInfo.setStdin(Arrays.asList(judgeConstraint.getInputs()));
        executeInfo.setStdout(outList);
        executeInfo.setStderr(errList);
        executeInfo.setTime(totalTime);
        executeInfo.setMemory(totalMemory);
        return Result.success(200,"执行成功",executeInfo);
    }


    /**
     * 普通判题
     */
    private static void normalJudge() {
        System.out.println("普通判题");
    }

}
