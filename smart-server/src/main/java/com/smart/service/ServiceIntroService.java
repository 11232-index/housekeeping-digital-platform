package com.smart.service;

import com.smart.dto.ServiceIntroDTO;
import com.smart.dto.ServiceIntroPageQueryDTO;
import com.smart.entity.ServiceIntro;
import com.smart.result.PageResult;

public interface ServiceIntroService {

    /**
     * 新增服务介绍
     */
    void save(ServiceIntroDTO serviceIntroDTO);

    /**
     * 分页查询
     */
    PageResult pageQuery(ServiceIntroPageQueryDTO dto);

    /**
     * 根据ID查询服务介绍
     */
    ServiceIntro getById(Long id);

    /**
     * 修改服务介绍
     */
    void update(ServiceIntroDTO serviceIntroDTO);

    /**
     * 删除服务介绍
     */
    void delete(Long id);

}
