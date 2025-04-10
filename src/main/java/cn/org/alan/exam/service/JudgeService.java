package cn.org.alan.exam.service;

import cn.org.alan.exam.constant.Constants;
import cn.org.alan.exam.constant.enums.JudgeTypeEnum;
import cn.org.alan.exam.pojo.Result;
import cn.org.alan.exam.pojo.dto.JudgeConstraint;
import cn.org.alan.exam.pojo.lang.LangContext;
import cn.org.alan.exam.utils.DockerUtil;
import cn.org.alan.exam.utils.FileUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JudgeService {
    public Result exeCode(JudgeConstraint judgeConstraint, LangContext langContext) {
        Result result = null;
        String folderPath = null;
        try {
            folderPath = Constants.codeSourcePath + "/" + UUID.randomUUID().toString();
            // 将代码保存
            FileUtil.saveFile(folderPath, langContext.getSourceFileName(), judgeConstraint.getCode());
            // 将测试用例保存(仅执行代码)
            if (JudgeTypeEnum.EXECUTE_CODE_ONLY.getState().equals(judgeConstraint.getType())) {
                for (int i = 0; i < judgeConstraint.getInputs().length; i++) {
                    FileUtil.saveFile(folderPath, i + ".in", judgeConstraint.getInputs()[i]);
                }
            }
            // 使用Docker进行编译、执行代码并返回输出结果
            result = DockerUtil.executeCodeWithDocker(judgeConstraint, langContext, folderPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 删除相关文件
            if (folderPath != null) FileUtil.delete(folderPath);
        }
        return result;
    }
}