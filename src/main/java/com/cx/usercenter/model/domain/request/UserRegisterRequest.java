package com.cx.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author cx
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 7249107058923182187L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
