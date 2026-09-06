package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务要求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequirement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    //服务项目id
    private Long serviceItemId;

    //要求名称
    private String name;

    //要求数据list
    private String value;

}
