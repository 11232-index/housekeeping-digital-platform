package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.constant.StatusConstant;
import com.smart.dto.ServiceItemDTO;
import com.smart.dto.ServiceItemPageQueryDTO;
import com.smart.entity.Category;
import com.smart.entity.ServiceItem;
import com.smart.entity.ServiceRequirement;
import com.smart.exception.BaseException;
import com.smart.mapper.CategoryMapper;
import com.smart.mapper.ServiceItemMapper;
import com.smart.result.PageResult;
import com.smart.service.ServiceItemService;
import com.smart.vo.ServiceItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ServiceItemServiceImpl implements ServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增服务项目
     *
     * @param serviceItemDTO
     */
    @Override
    @Transactional
    public void save(ServiceItemDTO serviceItemDTO) {
        // 1. DTO → Entity，设置默认可预约状态
        ServiceItem serviceItem = new ServiceItem();
        BeanUtils.copyProperties(serviceItemDTO, serviceItem);
        serviceItem.setStatus(StatusConstant.ENABLE);

        // 2. 插入 service_item 表（主键回填）
        serviceItemMapper.insert(serviceItem);

        // 3. 遍历 requirements，设置 serviceItemId 并插入 service_requirement 表
        List<ServiceRequirement> requirements = serviceItemDTO.getRequirements();
        if (requirements != null && !requirements.isEmpty()) {
            for (ServiceRequirement requirement : requirements) {
                requirement.setServiceItemId(serviceItem.getId());
                serviceItemMapper.insertRequirement(requirement);
            }
        }
    }

    /**
     * 分页查询
     *
     * @param serviceItemPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(ServiceItemPageQueryDTO serviceItemPageQueryDTO) {
        PageHelper.startPage(serviceItemPageQueryDTO.getPage(), serviceItemPageQueryDTO.getPageSize());
        Page<ServiceItemVO> page = serviceItemMapper.pageQuery(serviceItemPageQueryDTO);

        // 为每个服务填充服务需求列表
        List<ServiceItemVO> resultList = new ArrayList<>();
        for (ServiceItemVO vo : page.getResult()) {
            List<ServiceRequirement> reqs = serviceItemMapper.getRequirementsByServiceItemId(vo.getId());
            log.info("pageQuery - serviceId={}, reqCount={}", vo.getId(), reqs != null ? reqs.size() : 0);
            vo.setRequirements(reqs != null ? reqs : new ArrayList<>());
            resultList.add(vo);
        }
        return new PageResult(page.getTotal(), resultList);
    }

    /**
     * 根据ID查询服务信息
     *
     * @param id
     * @return
     */
    @Override
    public ServiceItemVO getById(Long id) {
        // 1. 查询 service_item
        ServiceItem serviceItem = serviceItemMapper.getById(id);
        if (serviceItem == null) {
            return null;
        }

        // 2. Entity → VO
        ServiceItemVO serviceItemVO = new ServiceItemVO();
        BeanUtils.copyProperties(serviceItem, serviceItemVO);

        // 3. 查询分类名称
        if (serviceItem.getCategoryId() != null) {
            Category category = categoryMapper.getById(serviceItem.getCategoryId());
            if (category != null) {
                serviceItemVO.setCategoryName(category.getName());
            }
        }

        // 4. 查询服务要求列表
        List<ServiceRequirement> requirements =
                serviceItemMapper.getRequirementsByServiceItemId(id);
        serviceItemVO.setRequirements(requirements != null ? requirements : new ArrayList<>());

        return serviceItemVO;
    }

    /**
     * 修改服务信息
     *
     * @param serviceItemDTO
     */
    @Override
    @Transactional
    public void update(ServiceItemDTO serviceItemDTO) {
        // 1. DTO → Entity，更新 service_item 表
        ServiceItem serviceItem = new ServiceItem();
        BeanUtils.copyProperties(serviceItemDTO, serviceItem);
        serviceItemMapper.update(serviceItem);

        // 2. 删除旧的 service_requirement
        serviceItemMapper.deleteRequirementsByServiceItemId(serviceItemDTO.getId());

        // 3. 插入新的 service_requirement
        List<ServiceRequirement> requirements = serviceItemDTO.getRequirements();
        if (requirements != null && !requirements.isEmpty()) {
            for (ServiceRequirement requirement : requirements) {
                requirement.setServiceItemId(serviceItemDTO.getId());
                serviceItemMapper.insertRequirement(requirement);
            }
        }
    }

    /**
     * 启用禁用服务
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        ServiceItem serviceItem = ServiceItem.builder()
                .status(status)
                .id(id)
                .build();
        serviceItemMapper.startOrStop(serviceItem);
    }

    /**
     * 删除服务
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 1. 查询服务是否存在
        ServiceItem serviceItem = serviceItemMapper.getById(id);
        if (serviceItem == null) {
            throw new BaseException("服务项目不存在");
        }

        // 2. 删除关联的 service_requirement
        serviceItemMapper.deleteRequirementsByServiceItemId(id);

        // 3. 删除 service_item 记录
        serviceItemMapper.delete(id);
    }

    /**
     * 批量删除服务（任何状态均可删除）
     *
     * @param ids
     */
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一项");
        }

        // 校验每个服务是否存在
        for (Long id : ids) {
            ServiceItem serviceItem = serviceItemMapper.getById(id);
            if (serviceItem == null) {
                throw new BaseException("服务项目不存在");
            }
        }

        // 删除每个服务的关联要求
        for (Long id : ids) {
            serviceItemMapper.deleteRequirementsByServiceItemId(id);
        }

        // 批量删除
        serviceItemMapper.deleteBatch(ids);
    }
}
