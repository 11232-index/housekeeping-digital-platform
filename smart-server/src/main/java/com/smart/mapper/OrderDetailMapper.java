package com.smart.mapper;

import com.smart.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细表数据访问层
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 新增订单明细
     * @param detail 订单明细实体
     */
    @Insert("INSERT INTO order_detail (order_id, housekeeper_id, service_item_id, " +
            "service_package_id, name, image, requirement, amount) " +
            "VALUES (#{orderId}, #{housekeeperId}, #{serviceItemId}, " +
            "#{servicePackageId}, #{name}, #{image}, #{requirement}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OrderDetail detail);

    /**
     * 批量新增订单明细
     * @param detailList 订单明细列表
     */
    void insertBatch(List<OrderDetail> detailList);

    /**
     * 根据订单id查询明细列表
     * @param orderId 订单id
     * @return 明细列表
     */
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId}")
    List<OrderDetail> listByOrderId(Long orderId);

    /**
     * 根据订单id删除所有明细
     * @param orderId 订单id
     */
    @Delete("DELETE FROM order_detail WHERE order_id = #{orderId}")
    void deleteByOrderId(Long orderId);
}
