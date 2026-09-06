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

    private String contract;

    private String healthCertificate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
