package com.smart.mapper;

import com.smart.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 新增购物车数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id和服务名称查询购物车数据（按name去重）
     */
    ShoppingCart getByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 根据用户id和服务项目id+服务要求查询购物车数据
     */
    ShoppingCart getByUserIdAndServiceItemIdAndRequirement(@Param("userId") Long userId, @Param("serviceItemId") Long serviceItemId, @Param("requirement") String requirement);

    /**
     * 根据用户id和服务套餐id+服务要求查询购物车数据
     */
    ShoppingCart getByUserIdAndServicePackageIdAndRequirement(@Param("userId") Long userId, @Param("servicePackageId") Long servicePackageId, @Param("requirement") String requirement);

    /**
     * 根据用户id查询购物车列表
     * @param userId
     * @return
     */
    List<ShoppingCart> listByUserId(Long userId);

    /**
     * 根据id删除单条购物车数据
     * @param id
     */
    void deleteById(Long id);

    /**
     * 根据用户id清空购物车
     * @param userId
     */
    void deleteByUserId(Long userId);
}
