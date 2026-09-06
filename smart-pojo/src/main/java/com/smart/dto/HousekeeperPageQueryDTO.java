package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 家政员分页查询DTO
 */
@Data
public class HousekeeperPageQueryDTO implements Serializable {

    /** 家政员名字（模糊搜索） */
    private String name;

    /** 页码 */
    private int page;

    /** 每页记录数 */
    private int pageSize;

}
