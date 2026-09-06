package com.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceSalesDTO implements Serializable {
    //服务名称
    private String name;

    //销量
    private Integer number;
}
