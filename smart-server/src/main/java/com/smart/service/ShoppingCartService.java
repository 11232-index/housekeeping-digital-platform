package com.smart.service;

import com.smart.dto.ShoppingCartDTO;
import com.smart.entity.ServiceRequirement;
import com.smart.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 新增购物车
     * @param shoppingCartDTO
     * @param userId
     */
    void add(ShoppingCartDTO shoppingCartDTO, Long userId);

    /**
     * 查询购物车列表
     * @param userId
     * @return
     */
    List<ShoppingCart> list(Long userId);

    /**
     * 减少购物车数量（数量>1减1，数量=1删除）
     * @param shoppingCartDTO
     * @param userId
     */
    void sub(ShoppingCartDTO shoppingCartDTO, Long userId);

    /**
     * 清空购物车
     * @param userId
     */
    void clean(Long userId);

    /**
     * 查询服务需求（根据服务项目id或服务套餐id）
     * @param serviceItemId
     * @param servicePackageId
     * @return
     */
    List<ServiceRequirement> getRequirements(Long serviceItemId, Long servicePackageId);
}
