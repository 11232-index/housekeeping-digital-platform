package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务推荐分页查询DTO
 */
@Data
public class HotPicksPageQueryDTO implements Serializable {

    //服务名称（模糊搜索）
    private String name;

    //状态 0关闭 1启用
    private Integer status;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
