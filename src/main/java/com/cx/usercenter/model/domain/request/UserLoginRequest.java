package com.cx.usercenter.model.domain.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户登录请求体
 * @author cx
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 6321858618361114934L;

    private String userAccount;

    private String userPassword;
}
