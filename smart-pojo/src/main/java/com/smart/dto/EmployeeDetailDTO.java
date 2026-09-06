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

    private String contract;

    private String healthCertificate;

    private String updateTime;

}
