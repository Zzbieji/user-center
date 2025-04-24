package com.cx.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cx.usercenter.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;



/**
 * 用户服务
 * @author cx
 */
@Service
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     * @param userAccount 登录账户
     * @param userPassword 登录密码
     * @param request 请求
     * @return 已登录用户信息
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser 原始用户信息
     * @return 脱敏后信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);
}
