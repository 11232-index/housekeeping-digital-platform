package com.smart.service.impl;

import com.smart.dto.ShoppingCartDTO;
import com.smart.entity.Category;
import com.smart.entity.ServiceItem;
import com.smart.entity.ServicePackage;
import com.smart.entity.ServicePackageItem;
import com.smart.entity.ServiceRequirement;
import com.smart.entity.ShoppingCart;
import com.smart.constant.MessageConstant;
import com.smart.exception.BaseException;
import com.smart.mapper.CategoryMapper;
import com.smart.mapper.ServiceItemMapper;
import com.smart.mapper.ServicePackageMapper;
import com.smart.mapper.ShoppingCartMapper;
import com.smart.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private ServicePackageMapper servicePackageMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增预约清单（按name去重）
     * @param shoppingCartDTO
     * @param userId
     */
    @Override
    @CacheEvict(value = "shoppingCart", key = "#userId")
    public void add(ShoppingCartDTO shoppingCartDTO, Long userId) {
        Long serviceItemId = shoppingCartDTO.getServiceItemId();
        Long servicePackageId = shoppingCartDTO.getServicePackageId();
        String requirement = shoppingCartDTO.getRequirement();

        String serviceName = null;

        // 判断是服务项目还是服务套餐
        if (serviceItemId != null) {
            // 校验服务项目是否存在及启用
            ServiceItem serviceItem = serviceItemMapper.getById(serviceItemId);
            if (serviceItem == null) {
                throw new BaseException("服务项目不存在");
            }
            if (serviceItem.getStatus() == 0) {
                throw new BaseException("该服务已下架，无法预约");
            }
            // 校验服务项目所属分类是否已启用
            if (serviceItem.getCategoryId() != null) {
                Category category = categoryMapper.getById(serviceItem.getCategoryId());
                if (category == null || category.getStatus() == 0) {
                    throw new BaseException("该服务所属分类已下架，无法预约");
                }
            }
            serviceName = serviceItem.getName();
        } else if (servicePackageId != null) {
            // 校验服务套餐是否存在及启用
            ServicePackage servicePackage = servicePackageMapper.getById(servicePackageId);
            if (servicePackage == null) {
                throw new BaseException("服务套餐不存在");
            }
            if (servicePackage.getStatus() == 0) {
                throw new BaseException("该套餐已下架，无法预约");
            }
            // 校验服务套餐所属分类是否已启用
            if (servicePackage.getCategoryId() != null) {
                Category category = categoryMapper.getById(servicePackage.getCategoryId());
                if (category == null || category.getStatus() == 0) {
                    throw new BaseException("该套餐所属分类已下架，无法预约");
                }
            }
            serviceName = servicePackage.getName();
        } else {
            throw new BaseException("服务项目id和服务套餐id不能同时为空");
        }

        // 按 name 判断是否已存在同名服务
        ShoppingCart existingCart = shoppingCartMapper.getByUserIdAndName(userId, serviceName);
        if (existingCart != null) {
            // 已存在同名服务，添加失败
            throw new BaseException(MessageConstant.SHOPPING_CART_ALREADY_EXISTS);
        }

        // 不存在同名服务，新增
        if (serviceItemId != null) {
            ServiceItem serviceItem = serviceItemMapper.getById(serviceItemId);
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .serviceItemId(serviceItemId)
                    .name(serviceItem.getName())
                    .image(serviceItem.getImage())
                    .requirement(requirement)
                    .amount(serviceItem.getPrice())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(shoppingCart);
            log.info("新增预约清单服务项目，id={}", shoppingCart.getId());
        } else {
            ServicePackage servicePackage = servicePackageMapper.getById(servicePackageId);
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .servicePackageId(servicePackageId)
                    .name(servicePackage.getName())
                    .image(servicePackage.getImage())
                    .requirement(requirement)
                    .amount(servicePackage.getPrice())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(shoppingCart);
            log.info("新增预约清单服务套餐，id={}", shoppingCart.getId());
        }
    }

    /**
     * 查询预约清单列表
     * @param userId
     * @return
     */
    @Override
    @Cacheable(value = "shoppingCart", key = "#userId", sync = true)
    public List<ShoppingCart> list(Long userId) {
        List<ShoppingCart> cartList = shoppingCartMapper.listByUserId(userId);
        log.info("查询预约清单列表，用户id={}，共{}条", userId, cartList.size());
        return cartList;
    }

    /**
     * 减少预约清单（直接删除）
     * @param shoppingCartDTO
     * @param userId
     */
    @Override
    @CacheEvict(value = "shoppingCart", key = "#userId")
    public void sub(ShoppingCartDTO shoppingCartDTO, Long userId) {
        Long serviceItemId = shoppingCartDTO.getServiceItemId();
        Long servicePackageId = shoppingCartDTO.getServicePackageId();
        String requirement = shoppingCartDTO.getRequirement();

        ShoppingCart existingCart = null;

        if (serviceItemId != null) {
            existingCart = shoppingCartMapper.getByUserIdAndServiceItemIdAndRequirement(userId, serviceItemId, requirement);
        } else if (servicePackageId != null) {
            existingCart = shoppingCartMapper.getByUserIdAndServicePackageIdAndRequirement(userId, servicePackageId, requirement);
        } else {
            throw new BaseException("服务项目id和服务套餐id不能同时为空");
        }

        if (existingCart == null) {
            throw new BaseException("预约清单中没有该服务");
        }

        // 直接删除
        shoppingCartMapper.deleteById(existingCart.getId());
        log.info("删除预约清单记录，id={}", existingCart.getId());
    }

    /**
     * 清空预约清单
     * @param userId
     */
    @Override
    @CacheEvict(value = "shoppingCart", key = "#userId")
    public void clean(Long userId) {
        shoppingCartMapper.deleteByUserId(userId);
        log.info("清空预约清单，用户id={}", userId);
    }

    /**
     * 查询服务需求（根据服务项目id或服务套餐id）
     * @param serviceItemId
     * @param servicePackageId
     * @return
     */
    @Override
    public List<ServiceRequirement> getRequirements(Long serviceItemId, Long servicePackageId) {
        if (serviceItemId != null) {
            List<ServiceRequirement> reqs = serviceItemMapper.getRequirementsByServiceItemId(serviceItemId);
            log.info("查询服务项目需求，serviceItemId={}，共{}条", serviceItemId, reqs != null ? reqs.size() : 0);
            return reqs != null ? reqs : Collections.emptyList();
        } else if (servicePackageId != null) {
            List<ServicePackageItem> items = servicePackageMapper.getPackageItemsByPackageId(servicePackageId);
            log.info("查询服务套餐需求，servicePackageId={}，共{}条", servicePackageId, items != null ? items.size() : 0);
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }
            return items.stream().map(item -> ServiceRequirement.builder()
                    .name(item.getName())
                    .value(item.getValue())
                    .build()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
