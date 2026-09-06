package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 关于我们分页查询DTO
 */
@Data
public class AboutPageQueryDTO implements Serializable {

    //题目（模糊搜索）
    private String title;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
