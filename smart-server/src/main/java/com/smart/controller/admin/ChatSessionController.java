package com.smart.controller.admin;

import com.smart.dto.ChatSessionDTO;
import com.smart.dto.ChatSessionPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI对话会话管理
 */
@RestController
@RequestMapping("/admin/chat")
@Slf4j
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 创建会话（首次提问时调用）
     */
    @PostMapping("/session")
    public Result<ChatSessionDTO> create(@RequestBody ChatSessionDTO chatSessionDTO) {
        log.info("创建对话会话：{}", chatSessionDTO);
        ChatSessionDTO result = chatSessionService.create(chatSessionDTO);
        return Result.success(result);
    }

    /**
     * 分页查询历史会话
     */
    @GetMapping("/session/page")
    public Result<PageResult> pageQuery(ChatSessionPageQueryDTO chatSessionPageQueryDTO) {
        log.info("分页查询对话会话，page：{}，pageSize：{}", chatSessionPageQueryDTO.getPage(), chatSessionPageQueryDTO.getPageSize());
        PageResult pageResult = chatSessionService.pageQuery(chatSessionPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 重命名会话
     */
    @PutMapping("/session/{id}")
    public Result updateTitle(@PathVariable Long id, @RequestBody ChatSessionDTO chatSessionDTO) {
        log.info("重命名对话会话，id：{}，新标题：{}", id, chatSessionDTO.getTitle());
        chatSessionService.updateTitle(id, chatSessionDTO.getTitle());
        return Result.success();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除对话会话，id：{}", id);
        chatSessionService.deleteById(id);
        return Result.success();
    }

    /**
     * 删除当前管理员所有会话
     */
    @DeleteMapping("/session")
    public Result deleteAll() {
        log.info("删除当前管理员所有对话会话");
        chatSessionService.deleteAll();
        return Result.success();
    }
}
