package cn.org.alan.exam.security.service;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.common.result.ResultCode;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.dto.UserAuthInfo;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.security.model.SysUserDetails;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 系统用户认证
 *
 * @author haoxr
 */
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    // private final SysUserService sysUserService;
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getUserName, username);
//        User user = userMapper.selectOne(wrapper);
//        if (Optional.ofNullable(user).isPresent()){
//            Result<Object> result = Result.failed(ResultCode.USER_NOT_EXIST);
//
//        }

        // UserAuthInfo userAuthInfo = sysUserService.getUserAuthInfo(username);
        // if (userAuthInfo == null) {
        //     throw new UsernameNotFoundException(username);
        // }
        // return new SysUserDetails(userAuthInfo);
        return null;

    }
}
