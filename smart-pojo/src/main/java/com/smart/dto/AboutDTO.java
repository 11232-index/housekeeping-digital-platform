package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 关于我们数据传输对象
 */
@Data
public class AboutDTO implements Serializable {

    private Long id;

    //题目
    private String title;

    //排序
    private Integer sortOrder;

    //关于我们内容
    private String content;

}
