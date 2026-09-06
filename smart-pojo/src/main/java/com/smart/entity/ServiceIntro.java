package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务介绍
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceIntro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //服务标题
    private String title;

    //图片路径
    private String picture;

    //服务内容
    private String content;

    //创建人
    private Long createUser;

    //修改人
    private Long updateUser;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

}
