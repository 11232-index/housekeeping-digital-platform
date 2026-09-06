package com.smart.service;

import com.smart.dto.ServiceItemDTO;
import com.smart.dto.ServiceItemPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.vo.ServiceItemVO;

import java.util.List;

public interface ServiceItemService {

    /**
     * 新增服务项目
     * @param serviceItemDTO
     */
    void save(ServiceItemDTO serviceItemDTO);

    /**
     * 分页查询
     * @param serviceItemPageQueryDTO
     * @return
     */
    PageResult pageQuery(ServiceItemPageQueryDTO serviceItemPageQueryDTO);

    /**
     * 根据ID查询服务信息
     * @param id
     * @return
     */
    ServiceItemVO getById(Long id);

    /**
     * 修改服务信息
     * @param serviceItemDTO
     */
    void update(ServiceItemDTO serviceItemDTO);

    /**
     * 启用禁用服务
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除服务
     * @param id
     */
    void delete(Long id);

    /**
     * 批量删除服务
     * @param ids
     */
    void batchDelete(List<Long> ids);
}
