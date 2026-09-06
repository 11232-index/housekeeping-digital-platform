package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 家政员关联员工信息 VO
 * 用于家政员详情页"下盒子"区域，展示家政员对应的员工表中的相关数据
 *
 * 数据来源：
 * - Employee 表（基础信息）
 * - EmployeeDetail 表（详细信息：学历、经验、职位等）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeeperEmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 来自 Employee 表 ====================

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String sex;

    /** 年龄 */
    private String age;

    // ==================== 来自 EmployeeDetail 表 ====================

    /** 学历（如：本科、大专、高中） */
    private String education;

    /** 过往经历（如：3年家政经验） */
    private String pastExperience;

    /** 职位（如：高级家政员） */
    private String position;

    /** 在职状态（如：在职、离职） */
    private String workStatus;

    /** 健康证（如：已办理、未办理） */
    private String healthCertificate;
}
