package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.pojo.system.LoginLog;
import com.crui.service.system.LoginLogService;
import com.crui.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    @Reference
    private LoginLogService loginLogService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        System.out.println("登录成功，记录日志");
        LoginLog loginLog = new LoginLog();
        String loginName = authentication.getName();
        loginLog.setLoginName(loginName);
        loginLog.setLoginTime(new Date());
        loginLog.setIp(httpServletRequest.getRemoteAddr());
        loginLog.setBrowserName(WebUtil.getBrowserName(httpServletRequest.getHeader("user-agent")));
        loginLog.setLocation(WebUtil.getCityByIP(httpServletRequest.getRemoteAddr()));
        loginLogService.add(loginLog);
        //登陆成功后跳转
        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);
    }
}
