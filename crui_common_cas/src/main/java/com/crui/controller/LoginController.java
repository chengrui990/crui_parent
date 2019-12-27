package com.crui.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @GetMapping("/username")
    public Map username(){
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("casLoginController,登录用户："+user);
        if ("anonymousUser".equals(user)){
            user = "";
        }
        Map map = new HashMap();
        map.put("username",user);
        return map;
    }
}
