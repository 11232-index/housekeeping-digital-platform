package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.context.BaseContext;
import com.smart.dto.ChatSessionDTO;
import com.smart.dto.ChatSessionPageQueryDTO;
import com.smart.entity.ChatSession;
import com.smart.mapper.ChatSessionMapper;
import com.smart.result.PageResult;
import com.smart.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Override
    public ChatSessionDTO create(ChatSessionDTO chatSessionDTO) {
        ChatSession session = new ChatSession();
        session.setAdminId(BaseContext.getCurrentId());
        session.setTitle(chatSessionDTO.getTitle());
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);
        chatSessionDTO.setId(session.getId());
        log.info("创建对话会话成功，id：{}，标题：{}", session.getId(), session.getTitle());
        return chatSessionDTO;
    }

    @Override
    public PageResult pageQuery(ChatSessionPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Long adminId = BaseContext.getCurrentId();
        Page<ChatSession> page = (Page<ChatSession>) chatSessionMapper.pageQuery(adminId, dto.getTitle());
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateTitle(Long id, String title) {
        ChatSession session = new ChatSession();
        session.setId(id);
        session.setTitle(title);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.update(session);
        log.info("重命名对话会话成功，id：{}，新标题：{}", id, title);
    }

    @Override
    public void deleteById(Long id) {
        chatSessionMapper.deleteById(id);
        log.info("删除对话会话成功，id：{}", id);
    }

    @Override
    public void deleteAll() {
        Long adminId = BaseContext.getCurrentId();
        chatSessionMapper.deleteAllByAdminId(adminId);
        log.info("删除当前管理员所有对话会话成功，adminId：{}", adminId);
    }
}
