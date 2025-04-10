package cn.org.alan.exam.pojo.lang;

import cn.org.alan.exam.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class PythonContext extends LangContext {
    // 编程语言
    private final String lang = "python";

    // 保存文件名
    private final String sourceFileName = "main.py";

    // 编程语言对应的docker镜像
    private final String image = "python:alpine";

    // 编程语言对应执行的编译命令
    private final String compileCmd = null;

    // 编程语言对应执行的启动命令
    private final String[] runCmd = {"python", Constants.containerVolumePath + "/" + "main.py"};
}