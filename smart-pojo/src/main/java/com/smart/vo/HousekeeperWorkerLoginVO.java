package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 家政员小程序端登录结果（员工表账号校验 + 家政员表头像/姓名）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeeperWorkerLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工id */
    private Long employeeId;

    /** 家政员id（可能为空，若该员工还没有 housekeeper 记录） */
    private Long housekeeperId;

    /** 家政员姓名（housekeeper.name 优先，缺失回退 employee.name） */
    private String name;

    /** 家政员头像地址（可能为空） */
    private String picture;

    /** 员工令牌（调用 /employee/** 等受保护接口时放请求头 token） */
    private String token;

}
