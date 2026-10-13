package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 家政员端"我的资料"聚合视图
 * <p>
 * 汇总三张表：employee（基本信息）+ employee_detail（详细信息、排班）
 * + housekeeper（单价/评分/身价/综合评价），再加上该家政员的可预约时段。
 * 其中 employee_detail 和 housekeeper 都可能不存在（该员工还没有详情记录 / 还没有家政员记录），
 * 对应字段为 null，由小程序端显示"暂无"。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 员工基本信息（employee） ====================

    /** 员工id */
    private Long employeeId;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 性别 1男 2女 */
    private String sex;

    /** 年龄 */
    private String age;

    // ==================== 员工详细信息（employee_detail，可能整块为空） ====================

    /** 学历（JSON 字符串，前端解析） */
    private String education;

    /** 过去经历（JSON 字符串，前端解析） */
    private String pastExperience;

    /** 岗位 */
    private String position;

    /** 工资 */
    private String salary;

    /** 工作状态 */
    private String workStatus;

    /** 排班（JSON 字符串，前端解析） */
    private String schedule;

    /** 合同文件路径（非空 = 已上传；小程序只判断有无，不渲染路径） */
    private String contract;

    /** 健康证文件路径（非空 = 已上传；同上） */
    private String healthCertificate;

    // ==================== 家政员信息（housekeeper，可能整块为空） ====================

    /** 家政员id */
    private Long housekeeperId;

    /** 头像地址 */
    private String picture;

    /** 单价 */
    private BigDecimal price;

    /** 身价 0无牌 1金牌 2银牌 3铜牌 */
    private Integer rank;

    /** 评分 */
    private BigDecimal rating;

    /** 综合评价 */
    private String review;

    // ==================== 可预约时段（status=1 且日期 >= 今天，按日期时间升序） ====================

    private List<WorkerScheduleSlotVO> availableSlots;

}
