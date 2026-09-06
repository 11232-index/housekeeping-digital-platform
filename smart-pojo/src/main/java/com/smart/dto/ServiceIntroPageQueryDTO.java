package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务介绍分页查询DTO
 */
@Data
public class ServiceIntroPageQueryDTO implements Serializable {

    //服务标题（模糊搜索）
    private String title;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
