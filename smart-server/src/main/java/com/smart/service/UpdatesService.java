package com.smart.service;

import com.smart.dto.UpdatesDTO;
import com.smart.dto.UpdatesPageQueryDTO;
import com.smart.entity.Updates;
import com.smart.result.PageResult;

public interface UpdatesService {

    /**
     * 新增动态消息
     * @param updatesDTO
     */
    void save(UpdatesDTO updatesDTO);

    /**
     * 分页查询
     * @param updatesPageQueryDTO
     * @return
     */
    PageResult pageQuery(UpdatesPageQueryDTO updatesPageQueryDTO);

    /**
     * 根据ID查询动态消息
     * @param id
     * @return
     */
    Updates getById(Long id);

    /**
     * 修改动态消息
     * @param updatesDTO
     */
    void update(UpdatesDTO updatesDTO);

    /**
     * 删除动态消息
     * @param id
     */
    void delete(Long id);

}
