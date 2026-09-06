package com.smart.vo;

import com.smart.entity.ServiceRequirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemVO implements Serializable {

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
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
    //服务项目关联的服务要求
    private List<ServiceRequirement> requirements = new ArrayList<>();

    //private Integer copies;
}
