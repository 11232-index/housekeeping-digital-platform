package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关于我们
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class About implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //题目
    private String title;

    //排序
    private Integer sortOrder;

    //关于我们内容
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
