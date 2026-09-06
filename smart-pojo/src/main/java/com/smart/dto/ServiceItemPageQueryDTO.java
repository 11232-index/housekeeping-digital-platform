package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServiceItemPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //分类id
    private Integer categoryId;

    //状态 0表示休息中 1表示可预约
    private Integer status;

}
