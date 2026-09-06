package com.smart.service.impl;

import com.smart.entity.ServiceIntroSearchHistory;
import com.smart.mapper.ServiceIntroSearchHistoryMapper;
import com.smart.service.ServiceIntroSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ServiceIntroSearchHistoryServiceImpl implements ServiceIntroSearchHistoryService {

    @Autowired
    private ServiceIntroSearchHistoryMapper mapper;

    @Override
    @CacheEvict(value = "serviceIntroSearchHistory", key = "#userId")
    public void save(String keyword, Long userId) {
        ServiceIntroSearchHistory history = ServiceIntroSearchHistory.builder()
                .userId(userId).keyword(keyword).createTime(LocalDateTime.now()).build();
        mapper.insert(history);
        log.info("新增服务介绍搜索记录，userId={}，keyword={}", userId, keyword);
    }

    @Override
    @Cacheable(value = "serviceIntroSearchHistory", key = "#userId", sync = true)
    public List<ServiceIntroSearchHistory> listByUserId(Long userId) {
        return mapper.listByUserId(userId);
    }

    @Override
    @CacheEvict(value = "serviceIntroSearchHistory", key = "#userId")
    public void clean(Long userId) {
        mapper.deleteByUserId(userId);
        log.info("清空服务介绍搜索记录，userId={}", userId);
    }

}
