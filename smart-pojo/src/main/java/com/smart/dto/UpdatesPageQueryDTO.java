package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态消息分页查询DTO
 */
@Data
public class UpdatesPageQueryDTO implements Serializable {

    //动态标题（模糊搜索）
    private String title;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
