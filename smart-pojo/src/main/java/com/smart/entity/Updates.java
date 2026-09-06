package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 动态消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Updates implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //动态标题
    private String title;

    //图片路径
    private String picture;

    //动态内容
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
