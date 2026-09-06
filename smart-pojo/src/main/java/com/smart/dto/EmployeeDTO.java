package com.smart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String sex;

    private String age;

    private String idNumber;

    private Integer status;

    private String role;

}
