package com.smart.service.impl;

import com.smart.entity.Feed;
import com.smart.mapper.FeedMapper;
import com.smart.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FeedServiceImpl implements FeedService {

    @Autowired
    private FeedMapper feedMapper;

    /** 新增feed */
    @Override
    public void save(Feed feed) {
        feedMapper.insert(feed);
        log.info("新增feed，updatesId={}, serviceIntroId={}", feed.getUpdatesId(), feed.getServiceIntroId());
    }

    /** 删除updates关联feed */
    @Override
    public void deleteByUpdatesId(Long updatesId) {
        feedMapper.deleteByUpdatesId(updatesId);
        log.info("删除feed，updatesId={}", updatesId);
    }

    /** 删除service_intro关联feed */
    @Override
    public void deleteByServiceIntroId(Long serviceIntroId) {
        feedMapper.deleteByServiceIntroId(serviceIntroId);
        log.info("删除feed，serviceIntroId={}", serviceIntroId);
    }

    /** 查询feed列表（联表），缓存 */
    @Override
    @Cacheable(value = "feed", key = "'list:' + #limit", sync = true)
    public List<Feed> list(int limit) {
        List<Feed> list = feedMapper.list(limit);
        log.debug("查询feed联表列表，共{}条", list.size());
        return list;
    }

}
