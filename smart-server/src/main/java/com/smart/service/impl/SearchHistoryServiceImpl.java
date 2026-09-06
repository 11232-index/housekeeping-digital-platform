package com.smart.service.impl;

import com.smart.entity.SearchHistory;
import com.smart.mapper.SearchHistoryMapper;
import com.smart.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    /**
     * 新增搜索记录
     */
    @Override
    @CacheEvict(value = "searchHistory", key = "#userId")
    public void save(String keyword, Long userId) {
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .keyword(keyword)
                .createTime(LocalDateTime.now())
                .build();
        searchHistoryMapper.insert(history);
        log.info("新增搜索记录，userId={}，keyword={}", userId, keyword);
    }

    /**
     * 查询用户搜索记录（最新10条，按用户缓存）
     */
    @Override
    @Cacheable(value = "searchHistory", key = "#userId", sync = true)
    public List<SearchHistory> listByUserId(Long userId) {
        return searchHistoryMapper.listByUserId(userId);
    }

    /**
     * 清空用户搜索记录
     */
    @Override
    @CacheEvict(value = "searchHistory", key = "#userId")
    public void clean(Long userId) {
        searchHistoryMapper.deleteByUserId(userId);
        log.info("清空搜索记录，userId={}", userId);
    }

}
