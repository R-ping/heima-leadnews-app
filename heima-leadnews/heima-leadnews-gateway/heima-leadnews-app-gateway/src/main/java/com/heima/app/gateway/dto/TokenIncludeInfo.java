package com.heima.app.gateway.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class TokenIncludeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码（BCryptPasswordEncoder加密，长度128）
     */
    private String password;

    /**
     * 用户名、手机号（全局唯一，用于登录和绑定）
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String image;

    /**
     * 0 男
     * 1 女
     * 2 未知
     */
    private Boolean sex;

    /**
     * 0 未
     * 1 是
     */
    private Boolean certification;

    /**
     * 是否身份认证
     */
    private Boolean identityAuthentication;

    /**
     * 1正常
     * 0锁定
     */
    private Boolean status;

    /**
     * 0 普通用户
     * 1 自媒体人
     * 2 大V
     */
    private Short flag;

    /**
     * 注册时间
     */
    private Date createdTime;

}