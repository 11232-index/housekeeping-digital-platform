package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.StatusConstant;
import com.smart.dto.ServicePackageDTO;
import com.smart.dto.ServicePackagePageQueryDTO;
import com.smart.entity.Category;
import com.smart.entity.ServicePackage;
import com.smart.entity.ServicePackageItem;
import com.smart.exception.BaseException;
import com.smart.mapper.CategoryMapper;
import com.smart.mapper.ServicePackageMapper;
import com.smart.result.PageResult;
import com.smart.service.ServicePackageService;
import com.smart.vo.ServicePackageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ServicePackageServiceImpl implements ServicePackageService {

    @Autowired
    private ServicePackageMapper servicePackageMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增服务套餐
     *
     * @param servicePackageDTO
     */
    @Override
    @Transactional
    public void save(ServicePackageDTO servicePackageDTO) {
        // 1. DTO → Entity，设置默认可预约状态
        ServicePackage servicePackage = new ServicePackage();
        BeanUtils.copyProperties(servicePackageDTO, servicePackage);
        servicePackage.setStatus(StatusConstant.ENABLE);

        // 2. 插入 service_package 表（主键回填）
        servicePackageMapper.insert(servicePackage);

        // 3. 遍历 servicePackageItems，设置 servicePackageId 并插入 service_package_item 表
        List<ServicePackageItem> items = servicePackageDTO.getServicePackageItems();
        if (items != null && !items.isEmpty()) {
            for (ServicePackageItem item : items) {
                item.setServicePackageId(servicePackage.getId());
                servicePackageMapper.insertPackageItem(item);
            }
        }
    }

    /**
     * 分页查询
     *
     * @param servicePackagePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(ServicePackagePageQueryDTO servicePackagePageQueryDTO) {
        PageHelper.startPage(servicePackagePageQueryDTO.getPage(), servicePackagePageQueryDTO.getPageSize());
        Page<ServicePackageVO> page = servicePackageMapper.pageQuery(servicePackagePageQueryDTO);

        // 为每个套餐填充套餐需求列表
        List<ServicePackageVO> resultList = new ArrayList<>();
        for (ServicePackageVO vo : page.getResult()) {
            List<ServicePackageItem> items = servicePackageMapper.getPackageItemsByPackageId(vo.getId());
            log.info("pageQuery package - packageId={}, itemCount={}", vo.getId(), items != null ? items.size() : 0);
            vo.setServicePackageItems(items != null ? items : new ArrayList<>());
            resultList.add(vo);
        }
        return new PageResult(page.getTotal(), resultList);
    }

    /**
     * 根据ID查询服务套餐信息
     *
     * @param id
     * @return
     */
    @Override
    public ServicePackageVO getById(Long id) {
        // 1. 查询 service_package
        ServicePackage servicePackage = servicePackageMapper.getById(id);
        if (servicePackage == null) {
            return null;
        }

        // 2. Entity → VO
        ServicePackageVO servicePackageVO = new ServicePackageVO();
        BeanUtils.copyProperties(servicePackage, servicePackageVO);

        // 3. 查询分类名称
        if (servicePackage.getCategoryId() != null) {
            Category category = categoryMapper.getById(servicePackage.getCategoryId());
            if (category != null) {
                servicePackageVO.setCategoryName(category.getName());
            }
        }

        // 4. 查询服务套餐要求列表
        List<ServicePackageItem> items =
                servicePackageMapper.getPackageItemsByPackageId(id);
        servicePackageVO.setServicePackageItems(items != null ? items : List.of());

        return servicePackageVO;
    }

    /**
     * 修改服务套餐信息
     *
     * @param servicePackageDTO
     */
    @Override
    @Transactional
    public void update(ServicePackageDTO servicePackageDTO) {
        // 1. DTO → Entity，更新 service_package 表
        ServicePackage servicePackage = new ServicePackage();
        BeanUtils.copyProperties(servicePackageDTO, servicePackage);
        servicePackageMapper.update(servicePackage);

        // 2. 删除旧的 service_package_item
        servicePackageMapper.deletePackageItemsByPackageId(servicePackageDTO.getId());

        // 3. 插入新的 service_package_item
        List<ServicePackageItem> items = servicePackageDTO.getServicePackageItems();
        if (items != null && !items.isEmpty()) {
            for (ServicePackageItem item : items) {
                item.setServicePackageId(servicePackageDTO.getId());
                servicePackageMapper.insertPackageItem(item);
            }
        }
    }

    /**
     * 启用禁用服务套餐
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        ServicePackage servicePackage = ServicePackage.builder()
                .status(status)
                .id(id)
                .build();
        servicePackageMapper.startOrStop(servicePackage);
    }

    /**
     * 删除服务套餐
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 1. 查询套餐是否存在
        ServicePackage servicePackage = servicePackageMapper.getById(id);
        if (servicePackage == null) {
            throw new BaseException("服务套餐不存在");
        }

        // 2. 删除关联的 service_package_item
        servicePackageMapper.deletePackageItemsByPackageId(id);

        // 3. 删除 service_package 记录
        servicePackageMapper.delete(id);
    }

    /**
     * 批量删除服务套餐（任何状态均可删除）
     *
     * @param ids
     */
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一项");
        }

        // 校验每个套餐是否存在
        for (Long id : ids) {
            ServicePackage servicePackage = servicePackageMapper.getById(id);
            if (servicePackage == null) {
                throw new BaseException("服务套餐不存在");
            }
        }

        // 删除每个套餐的关联要求
        for (Long id : ids) {
            servicePackageMapper.deletePackageItemsByPackageId(id);
        }

        // 批量删除
        servicePackageMapper.deleteBatch(ids);
    }
}
