package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddressBookPageQueryDTO implements Serializable {

    //页码
    private int page;

    //每页记录数
    private int pageSize;

    //联系人（可选，用于模糊搜索）
    private String consignee;

}
