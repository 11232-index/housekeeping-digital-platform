package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long employeeId;

    private String education;

    private String pastExperience;

    private String position;

    private String salary;

    private String workStatus;

    /**
     * 排班（JSON）：key 是星期（1=周一 … 7=周日），value 是该天自己的一条或多条时段
     * 例：{"1":[{"start":"09:00","end":"11:00"}],"3":[{"start":"14:00","end":"16:00"}]}
     * 没有 key 的星期表示不排班；无排班时为空字符串
     */
    private String schedule;

    private String contract;

    private String healthCertificate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
