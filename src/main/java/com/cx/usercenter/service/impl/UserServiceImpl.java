package com.cx.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cx.usercenter.common.ErrorCode;
import com.cx.usercenter.exception.BusinessException;
import com.cx.usercenter.mapper.UserMapper;
import com.cx.usercenter.model.domain.User;
import com.cx.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.cx.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 * @author cx
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 加密盐值
     */
    private static final String SALT = "cx";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //校验
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length() < 8||checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        if(planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //账户不能包含特殊字符
        String pattern = "^[a-zA-Z0-9_]+$";
        if(!userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        count=this.lambdaQuery().eq(User::getPlanetCode, planetCode).count();
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }
        //密码相同
        if(!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult=this.save(user);
        if(!saveResult) {
           throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }
        return user.getId();
    }

    @Override
    public Long doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //账户不能包含特殊字符
        String pattern = "^[a-zA-Z0-9_]+$";
        if(!userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        //查询用户是否存在
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        User user=this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword).one();
        //用户不存在
        if(user==null) {
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user.getId());
        return user.getId();
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        User safetyUsesr=new User();
        safetyUsesr.setId(originUser.getId());
        safetyUsesr.setUsername(originUser.getUsername());
        safetyUsesr.setUserAccount(originUser.getUserAccount());
        safetyUsesr.setAvatarUrl(originUser.getAvatarUrl());
        safetyUsesr.setGender(originUser.getGender());
        safetyUsesr.setPhone(originUser.getPhone());
        safetyUsesr.setEmail(originUser.getEmail());
        safetyUsesr.setRole(originUser.getRole());
        safetyUsesr.setPlanetCode(originUser.getPlanetCode());
        safetyUsesr.setUserStatus(originUser.getUserStatus());
        safetyUsesr.setCreateTime(originUser.getCreateTime());
        return safetyUsesr;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Cacheable(value = "user", key = "#id")
    @Override
    public User getUserByIdFromCache(Long id) {
        return this.getById(id);
    }
}
