package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 最新动态搜索记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatesSearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String keyword;

    private LocalDateTime createTime;

}
