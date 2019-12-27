package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.entity.Result;
import com.crui.pojo.user.User;
import com.crui.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @GetMapping("sendSms")
    public Result sendSms(String phone){
        userService.sendSms(phone);
        return new Result();
    }

    @PostMapping("/save")
    public Result save(@RequestBody User user, String smsCode){
        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String newPass = encoder.encode(user.getPassword());
        System.out.println("passwprd:"+newPass);
        user.setPassword(newPass);

        userService.add(user,smsCode);
        return new Result();
    }
}
