package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态消息数据传输对象
 */
@Data
public class UpdatesDTO implements Serializable {

    private Long id;

    //动态标题
    private String title;

    //图片路径
    private String picture;

    //动态内容
    private String content;

}
