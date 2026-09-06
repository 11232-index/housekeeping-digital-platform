package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.AboutDTO;
import com.smart.dto.AboutPageQueryDTO;
import com.smart.entity.About;
import com.smart.mapper.AboutMapper;
import com.smart.result.PageResult;
import com.smart.service.AboutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AboutServiceImpl implements AboutService {

    @Autowired
    private AboutMapper aboutMapper;

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public void save(AboutDTO aboutDTO) {
        About about = new About();
        BeanUtils.copyProperties(aboutDTO, about);
        aboutMapper.insert(about);
        log.info("新增关于我们，id={}", about.getId());
    }

    @Override
    @Cacheable(value = "about", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.title", sync = true)
    public PageResult pageQuery(AboutPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<About> page = aboutMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Cacheable(value = "about", key = "'one:' + #id", sync = true)
    public About getById(Long id) {
        return aboutMapper.getById(id);
    }

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public void update(AboutDTO aboutDTO) {
        About about = new About();
        BeanUtils.copyProperties(aboutDTO, about);
        aboutMapper.update(about);
        log.info("修改关于我们，id={}", about.getId());
    }

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public void delete(Long id) {
        aboutMapper.delete(id);
        log.info("删除关于我们，id={}", id);
    }

}
