package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.HotPicksDTO;
import com.smart.dto.HotPicksPageQueryDTO;
import com.smart.entity.HotPicks;
import com.smart.mapper.HotPicksMapper;
import com.smart.result.PageResult;
import com.smart.service.HotPicksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HotPicksServiceImpl implements HotPicksService {

    @Autowired
    private HotPicksMapper hotPicksMapper;

    @Override
    @CacheEvict(value = "hotPicks", allEntries = true)
    public void save(HotPicksDTO hotPicksDTO) {
        HotPicks hotPicks = new HotPicks();
        BeanUtils.copyProperties(hotPicksDTO, hotPicks);
        hotPicksMapper.insert(hotPicks);
        log.info("新增服务推荐，id={}", hotPicks.getId());
    }

    @Override
    @Cacheable(value = "hotPicks", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.name + ':' + #dto.status", sync = true)
    public PageResult pageQuery(HotPicksPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<HotPicks> page = hotPicksMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Cacheable(value = "hotPicks", key = "'one:' + #id", sync = true)
    public HotPicks getById(Long id) {
        return hotPicksMapper.getById(id);
    }

    @Override
    @CacheEvict(value = "hotPicks", allEntries = true)
    public void update(HotPicksDTO hotPicksDTO) {
        HotPicks hotPicks = new HotPicks();
        BeanUtils.copyProperties(hotPicksDTO, hotPicks);
        hotPicksMapper.update(hotPicks);
        log.info("修改服务推荐，id={}", hotPicks.getId());
    }

    @Override
    @CacheEvict(value = "hotPicks", allEntries = true)
    public void startOrStop(Integer status, Long id) {
        HotPicks hotPicks = HotPicks.builder().status(status).id(id).build();
        hotPicksMapper.startOrStop(hotPicks);
        log.info("启用/禁用服务推荐，id={}, status={}", id, status);
    }

    @Override
    @CacheEvict(value = "hotPicks", allEntries = true)
    public void delete(Long id) {
        hotPicksMapper.delete(id);
        log.info("删除服务推荐，id={}", id);
    }

}
