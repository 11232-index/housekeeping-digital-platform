package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务介绍数据传输对象
 */
@Data
public class ServiceIntroDTO implements Serializable {

    private Long id;

    //服务标题
    private String title;

    //图片路径
    private String picture;

    //服务内容
    private String content;

}
