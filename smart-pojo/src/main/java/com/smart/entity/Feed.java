package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 资讯动态统一表（联表updates和service_intro，不冗余title等字段） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feed implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long updatesId;
    private Long serviceIntroId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ── 以下为联表查询时填充 ──
    /** 来源类型 updates / service-intro */
    private String sourceType;
    /** 标题 */
    private String title;
    /** 图片 */
    private String picture;
    /** 内容 */
    private String content;

}
