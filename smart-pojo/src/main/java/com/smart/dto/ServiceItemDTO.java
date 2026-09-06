package com.smart.dto;

import com.smart.entity.ServiceRequirement;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceItemDTO implements Serializable {

    private Long id;
    //服务项目名称
    private String name;
    //服务项目分类id
    private Long categoryId;
    //服务项目价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 休息中 1 可预约
    private Integer status;
    //服务要求
    private List<ServiceRequirement> requirements = new ArrayList<>();

}
