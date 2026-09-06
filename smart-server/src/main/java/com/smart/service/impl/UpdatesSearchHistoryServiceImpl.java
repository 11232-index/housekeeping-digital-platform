package com.smart.service.impl;

import com.smart.entity.UpdatesSearchHistory;
import com.smart.mapper.UpdatesSearchHistoryMapper;
import com.smart.service.UpdatesSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UpdatesSearchHistoryServiceImpl implements UpdatesSearchHistoryService {

    @Autowired
    private UpdatesSearchHistoryMapper mapper;

    @Override
    @CacheEvict(value = "updatesSearchHistory", key = "#userId")
    public void save(String keyword, Long userId) {
        UpdatesSearchHistory history = UpdatesSearchHistory.builder()
                .userId(userId).keyword(keyword).createTime(LocalDateTime.now()).build();
        mapper.insert(history);
        log.info("新增最新动态搜索记录，userId={}，keyword={}", userId, keyword);
    }

    @Override
    @Cacheable(value = "updatesSearchHistory", key = "#userId", sync = true)
    public List<UpdatesSearchHistory> listByUserId(Long userId) {
        return mapper.listByUserId(userId);
    }

    @Override
    @CacheEvict(value = "updatesSearchHistory", key = "#userId")
    public void clean(Long userId) {
        mapper.deleteByUserId(userId);
        log.info("清空最新动态搜索记录，userId={}", userId);
    }

}
