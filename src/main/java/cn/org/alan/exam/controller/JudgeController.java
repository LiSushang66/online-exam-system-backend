package cn.org.alan.exam.controller;

import cn.org.alan.exam.annotation.GlobalInterceptor;
import cn.org.alan.exam.annotation.VerifyParam;
import cn.org.alan.exam.constant.enums.LangContextEnum;
import cn.org.alan.exam.exception.BusinessException;
import cn.org.alan.exam.pojo.Result;
import cn.org.alan.exam.pojo.dto.JudgeConstraint;
import cn.org.alan.exam.service.JudgeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "代码执行接口")
@RestController
@RequestMapping("/api/code")
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @PostMapping("/execode")
    @GlobalInterceptor
    @ApiOperation("执行代码")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result exeCode(@VerifyParam JudgeConstraint judgeConstraint) {
        // 校验语言
        LangContextEnum langContextEnum = LangContextEnum.getByLang(judgeConstraint.getLang().toLowerCase());
        if(langContextEnum == null) {
            throw new BusinessException(
                    Result.error(400,"暂不支持" + judgeConstraint.getLang() + "语言"));
        }
        return judgeService.exeCode(judgeConstraint,langContextEnum.getLangContext());
    }
}
