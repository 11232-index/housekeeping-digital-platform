package com.smart.service;

import com.smart.entity.SearchHistory;

import java.util.List;

public interface SearchHistoryService {

    /**
     * 新增搜索记录（已存在则更新时间为当前）
     */
    void save(String keyword, Long userId);

    /**
     * 查询用户搜索记录
     */
    List<SearchHistory> listByUserId(Long userId);

    /**
     * 清空用户搜索记录
     */
    void clean(Long userId);

}
