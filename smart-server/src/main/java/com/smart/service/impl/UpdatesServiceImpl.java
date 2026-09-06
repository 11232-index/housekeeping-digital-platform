package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.UpdatesDTO;
import com.smart.dto.UpdatesPageQueryDTO;
import com.smart.entity.Feed;
import com.smart.entity.Updates;
import com.smart.mapper.UpdatesMapper;
import com.smart.result.PageResult;
import com.smart.service.FeedService;
import com.smart.service.UpdatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdatesServiceImpl implements UpdatesService {

    @Autowired
    private UpdatesMapper updatesMapper;

    @Autowired
    private FeedService feedService;

    /** 新增动态消息（同步feed外键） */
    @Caching(evict = {
            @CacheEvict(value = "updates", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    @Override
    public void save(UpdatesDTO updatesDTO) {
        Updates updates = new Updates();
        BeanUtils.copyProperties(updatesDTO, updates);
        updatesMapper.insert(updates);
        log.info("新增动态消息，id={}", updates.getId());
        feedService.save(Feed.builder()
                .updatesId(updates.getId())
                .createTime(updates.getCreateTime())
                .updateTime(updates.getUpdateTime())
                .build());
    }

    @Override
    @Cacheable(value = "updates", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.title", sync = true)
    public PageResult pageQuery(UpdatesPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Updates> page = updatesMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Cacheable(value = "updates", key = "'one:' + #id", sync = true)
    public Updates getById(Long id) {
        return updatesMapper.getById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "updates", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    public void update(UpdatesDTO updatesDTO) {
        Updates updates = new Updates();
        BeanUtils.copyProperties(updatesDTO, updates);
        updatesMapper.update(updates);
        log.info("修改动态消息，id={}", updates.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "updates", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    public void delete(Long id) {
        updatesMapper.delete(id);
        log.info("删除动态消息，id={}", id);
        feedService.deleteByUpdatesId(id);
    }

}
