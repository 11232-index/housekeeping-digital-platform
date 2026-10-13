package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDetailDTO implements Serializable {

    private Long id;

    private Long employeeId;

    private String education;

    private String pastExperience;

    private String position;

    private String salary;

    private String workStatus;

    /**
     * 排班（JSON 字符串，结构同 EmployeeDetail.schedule）
     * {"1":[{"start":"09:00","end":"11:00"}],"3":[...]}，key 为星期 1=周一 … 7=周日
     */
    private String schedule;

    private String contract;

    private String healthCertificate;

    private String updateTime;

}
