package com.smart.service.impl;

import com.smart.entity.HousekeeperSearchHistory;
import com.smart.mapper.HousekeeperSearchHistoryMapper;
import com.smart.service.HousekeeperSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class HousekeeperSearchHistoryServiceImpl implements HousekeeperSearchHistoryService {

    @Autowired
    private HousekeeperSearchHistoryMapper mapper;

    @Override
    @CacheEvict(value = "housekeeperSearchHistory", key = "#userId")
    public void save(String keyword, Long userId) {
        mapper.insert(HousekeeperSearchHistory.builder().userId(userId).keyword(keyword).createTime(LocalDateTime.now()).build());
        log.info("新增家政员搜索记录，userId={}，keyword={}", userId, keyword);
    }

    @Override
    @Cacheable(value = "housekeeperSearchHistory", key = "#userId", sync = true)
    public List<HousekeeperSearchHistory> listByUserId(Long userId) {
        return mapper.listByUserId(userId);
    }

    @Override
    @CacheEvict(value = "housekeeperSearchHistory", key = "#userId")
    public void clean(Long userId) {
        mapper.deleteByUserId(userId);
        log.info("清空家政员搜索记录，userId={}", userId);
    }

}
