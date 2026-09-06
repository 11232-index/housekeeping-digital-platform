package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务推荐数据传输对象
 */
@Data
public class HotPicksDTO implements Serializable {

    private Long id;

    //服务名称
    private String name;

    //图片路径
    private String picture;

    //状态 0关闭 1启用
    private Integer status;

    //排序
    private Integer sortOrder;

}
