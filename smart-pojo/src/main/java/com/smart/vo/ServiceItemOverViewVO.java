package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务项目总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemOverViewVO implements Serializable {
    // 可预约数量
    private Integer sold;

    // 休息中数量
    private Integer discontinued;
}
