package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务推荐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotPicks implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //服务名称
    private String name;

    //图片路径
    private String picture;

    //状态 0关闭 1启用
    private Integer status;

    //排序
    private Integer sortOrder;

    //创建人
    private Long createUser;

    //修改人
    private Long updateUser;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

}
