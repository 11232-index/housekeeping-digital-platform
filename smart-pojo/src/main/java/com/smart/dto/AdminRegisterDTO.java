package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员注册DTO
 */
@Data
public class AdminRegisterDTO implements Serializable {

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String name;

    /** 密码 */
    private String password;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String sex;

    /** 年龄 */
    private String age;

    /** 身份证号 */
    private String idNumber;
}
