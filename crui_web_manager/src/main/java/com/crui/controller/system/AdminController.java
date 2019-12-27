package com.crui.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.entity.PageResult;
import com.crui.entity.Result;
import com.crui.pojo.system.Admin;
import com.crui.service.system.AdminService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Admin findById(Integer id){
        return adminService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Map<String,Object> map){
        System.out.println(map);
        String newPass = map.get("newPass").toString();
        String loginName = map.get("loginName").toString();
        String checkPass = map.get("checkPass").toString();
        String oldPass = map.get("oldPass").toString();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!name.equals(loginName)){
            throw new RuntimeException("用户不一致");
        }
        if (newPass==null || checkPass==null || oldPass==null){
            throw new RuntimeException("密码不能为空");
        }
        if (!newPass.equals(checkPass)){
            throw new RuntimeException("两次输入密码不一致");
        }
        try {
            adminService.update(map);
        }catch (Exception e){
            return new Result(1, e.getMessage());
        }

        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }


}
