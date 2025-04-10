package cn.org.alan.exam.pojo.lang;

import cn.org.alan.exam.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JsContext extends LangContext {
    // 编程语言
    private final String lang = "javascript";

    // 保存文件名
    private final String sourceFileName = "main.js";

    // 编程语言对应的docker镜像
    private final String image = "node:alpine";

    // 编程语言对应执行的编译命令
    private final String compileCmd = null;

    // 编程语言对应执行的启动命令
    private final String[] runCmd = {"node", Constants.containerVolumePath + "/" + "main.js"};
}
