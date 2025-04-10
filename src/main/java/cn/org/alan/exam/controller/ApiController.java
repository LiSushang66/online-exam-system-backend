package cn.org.alan.exam.controller;

import cn.org.alan.exam.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "代码执行接口")
@RestController
@RequestMapping("/api/code")
public class ApiController {

    /**
     * 获取代码执行权限
     */
    @GetMapping("/getApiKey")
    @ApiOperation("获取代码执行权限")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result getApiKey() {
        Map<String, Object> map = new HashMap<>();
        return Result.success(200, "获取代码执行权限成功", map);
    }
}
