package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务套餐要求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    //服务套餐id
    private Long servicePackageId;

    //要求名称
    private String name;

    //要求数据list
    private String value;

}
