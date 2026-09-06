package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionPageQueryDTO implements Serializable {

    private String title;

    private int page;

    private int pageSize;
}
