package com.cx.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cx.usercenter.common.BaseResponse;
import com.cx.usercenter.common.ErrorCode;
import com.cx.usercenter.common.ResultUtils;
import com.cx.usercenter.exception.BusinessException;
import com.cx.usercenter.model.domain.User;
import com.cx.usercenter.model.domain.request.UserLoginRequest;
import com.cx.usercenter.model.domain.request.UserRegisterRequest;
import com.cx.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.cx.usercenter.common.ErrorCode.*;
import static com.cx.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.cx.usercenter.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 * @author cx
 */
@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest==null){
            throw new BusinessException(PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest==null){
            throw new BusinessException(PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(PARAMS_ERROR);
        }
        User user=userService.doLogin(userAccount, userPassword,request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request==null){
            throw new BusinessException(PARAMS_ERROR);
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request) {
        if(!isAdmin(request)){
            throw new BusinessException(NO_AUTH);
        }
        log.info(username);
        List<User> userList = userService.lambdaQuery()
                .like(StringUtils.isNotBlank(username), User::getUsername, username)
                .list();
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request) {
        if(!isAdmin(request)){
            throw new BusinessException(NO_AUTH);
        }
        if(id<=0){
            throw new BusinessException(PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 是否为管理员
     * @param request 请求
     * @return 是否
     */
    private boolean isAdmin(HttpServletRequest request){
        //鉴权
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user =(User)userObj;
        return user != null && user.getRole() == ADMIN_ROLE;
    }
}
