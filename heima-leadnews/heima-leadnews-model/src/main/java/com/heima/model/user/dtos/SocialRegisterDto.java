package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 社交登录注册新账号请求 DTO
 * <p>
 * "没有账号，请完善资料" 场景使用
 */
@Data
public class SocialRegisterDto {

    /**
     * 临时凭证（SocialAuth接口返回的tempToken）
     */
    private String tempToken;

    /**
     * 新用户名
     */
    private String name;

    /**
     * 密码（明文，后端用BCrypt加密存储）
     */
    private String password;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 邮箱（可选）
     */
    private String email;
}
