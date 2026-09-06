package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.ServiceIntroDTO;
import com.smart.dto.ServiceIntroPageQueryDTO;
import com.smart.entity.Feed;
import com.smart.entity.ServiceIntro;
import com.smart.mapper.ServiceIntroMapper;
import com.smart.result.PageResult;
import com.smart.service.FeedService;
import com.smart.service.ServiceIntroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServiceIntroServiceImpl implements ServiceIntroService {

    @Autowired
    private ServiceIntroMapper serviceIntroMapper;

    @Autowired
    private FeedService feedService;

    /** 新增服务介绍（同步feed外键） */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "serviceIntro", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    public void save(ServiceIntroDTO serviceIntroDTO) {
        ServiceIntro serviceIntro = new ServiceIntro();
        BeanUtils.copyProperties(serviceIntroDTO, serviceIntro);
        serviceIntroMapper.insert(serviceIntro);
        log.info("新增服务介绍，id={}", serviceIntro.getId());
        // 同步到feed（只存外键+时间，联表查询时JOIN拿title等字段）
        feedService.save(Feed.builder()
                .serviceIntroId(serviceIntro.getId())
                .createTime(serviceIntro.getCreateTime())
                .updateTime(serviceIntro.getUpdateTime())
                .build());
    }

    @Override
    @Cacheable(value = "serviceIntro", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.title", sync = true)
    public PageResult pageQuery(ServiceIntroPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<ServiceIntro> page = serviceIntroMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Cacheable(value = "serviceIntro", key = "'one:' + #id", sync = true)
    public ServiceIntro getById(Long id) {
        return serviceIntroMapper.getById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "serviceIntro", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    public void update(ServiceIntroDTO serviceIntroDTO) {
        ServiceIntro serviceIntro = new ServiceIntro();
        BeanUtils.copyProperties(serviceIntroDTO, serviceIntro);
        serviceIntroMapper.update(serviceIntro);
        log.info("修改服务介绍，id={}", serviceIntro.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "serviceIntro", allEntries = true),
            @CacheEvict(value = "feed", allEntries = true)
    })
    public void delete(Long id) {
        serviceIntroMapper.delete(id);
        log.info("删除服务介绍，id={}", id);
        feedService.deleteByServiceIntroId(id);
    }

}
