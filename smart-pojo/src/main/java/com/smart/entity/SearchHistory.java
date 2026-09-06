package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户id
    private Long userId;

    //搜索关键词
    private String keyword;

    //创建时间
    private LocalDateTime createTime;

}
