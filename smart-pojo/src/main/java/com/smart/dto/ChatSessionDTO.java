package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionDTO implements Serializable {

    private Long id;

    private Long adminId;

    private String title;
}
