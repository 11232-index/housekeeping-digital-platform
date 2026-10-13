package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.result.Result;
import com.smart.service.UserNotifyService;
import com.smart.vo.UserNotifyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 - 消息中心
 */
@RestController
@RequestMapping("/user/notify")
@Slf4j
public class UserNotifyController {

    @Autowired
    private UserNotifyService userNotifyService;

    /**
     * 消息列表（默认最近 30 条）
     */
    @GetMapping("/list")
    public Result<List<UserNotifyVO>> list(@RequestParam(defaultValue = "30") int limit) {
        return Result.success(userNotifyService.listRecent(BaseContext.getCurrentId(), limit));
    }

    /**
     * 未读消息数（小程序红点）
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(userNotifyService.countUnread(BaseContext.getCurrentId()));
    }

    /**
     * 标记单条已读
     */
    @PostMapping("/{id}/read")
    public Result markRead(@PathVariable Long id) {
        userNotifyService.markRead(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    /**
     * 一键全部已读
     */
    @PostMapping("/read-all")
    public Result markAllRead() {
        userNotifyService.markAllRead(BaseContext.getCurrentId());
        return Result.success();
    }
}
