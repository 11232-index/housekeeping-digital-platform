package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工端工作台 - 待办事项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkbenchTodoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 待接单（status=2） */
    private Long pendingAccept;

    /** 待指派（status=2 且尚未指派家政员） */
    private Long pendingAssign;

    /** 待退款（status=6） */
    private Long refunding;

}
