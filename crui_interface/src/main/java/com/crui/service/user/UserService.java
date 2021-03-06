package com.crui.service.user;
import com.crui.entity.PageResult;
import com.crui.pojo.user.User;

import java.util.*;

/**
 * user业务逻辑层
 */
public interface UserService {


    public List<User> findAll();


    public PageResult<User> findPage(int page, int size);


    public List<User> findList(Map<String,Object> searchMap);


    public PageResult<User> findPage(Map<String,Object> searchMap,int page, int size);


    public User findById(String username);

    public void add(User user);


    public void update(User user);


    public void delete(String username);

    public void sendSms(String phone);

    /**
     * 用户注册
     * @param user
     * @param smsCode 验证码
     */
    public void add(User user, String smsCode);

}
