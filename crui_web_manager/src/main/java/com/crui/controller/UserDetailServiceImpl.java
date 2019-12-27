package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.pojo.system.Admin;
import com.crui.service.system.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailServiceImpl implements UserDetailsService {
    @Reference
    private AdminService adminService;
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println("UserDetailServiceImpl............");
        Map map = new HashMap<String, String>();
        map.put("loginName", s);
        map.put("status", "1");
        List<Admin> adminList = adminService.findList(map);
        if (null == adminList || adminList.size()==0){
            return  null;
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new User(s, adminList.get(0).getPassword(),grantedAuthorities);
    }
}
