package com.smart.service;

import com.smart.dto.ServicePackageDTO;
import com.smart.dto.ServicePackagePageQueryDTO;
import com.smart.result.PageResult;
import com.smart.vo.ServicePackageVO;

import java.util.List;

public interface ServicePackageService {

    /**
     * 新增服务套餐
     * @param servicePackageDTO
     */
    void save(ServicePackageDTO servicePackageDTO);

    /**
     * 分页查询
     * @param servicePackagePageQueryDTO
     * @return
     */
    PageResult pageQuery(ServicePackagePageQueryDTO servicePackagePageQueryDTO);

    /**
     * 根据ID查询服务套餐信息
     * @param id
     * @return
     */
    ServicePackageVO getById(Long id);

    /**
     * 修改服务套餐信息
     * @param servicePackageDTO
     */
    void update(ServicePackageDTO servicePackageDTO);

    /**
     * 启用禁用服务套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除服务套餐
     * @param id
     */
    void delete(Long id);

    /**
     * 批量删除服务套餐
     * @param ids
     */
    void batchDelete(List<Long> ids);
}
