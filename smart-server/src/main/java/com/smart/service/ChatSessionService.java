package com.smart.service;

import com.smart.dto.ChatSessionDTO;
import com.smart.dto.ChatSessionPageQueryDTO;
import com.smart.result.PageResult;

public interface ChatSessionService {

    /**
     * 创建会话（首次提问时调用，保存第一个问题作为标题）
     */
    ChatSessionDTO create(ChatSessionDTO chatSessionDTO);

    /**
     * 分页查询当前管理员的历史会话
     */
    PageResult pageQuery(ChatSessionPageQueryDTO chatSessionPageQueryDTO);

    /**
     * 重命名会话标题
     */
    void updateTitle(Long id, String title);

    /**
     * 根据ID删除会话
     */
    void deleteById(Long id);

    /**
     * 删除当前管理员的所有会话
     */
    void deleteAll();
}
