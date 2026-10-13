package com.smart.mapper;

import com.smart.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 重新指派家政员时，同步更新明细上记录的家政员信息（姓名与id）。
     * <p>
     * 只更新「本来就带家政员」的明细（housekeeper_id 非空 = 家政员订单），
     * 服务订单的明细 housekeeper_id 为空、name 存的是服务名，不能被覆盖。
     * <p>
     * 更新后 housekeeper_id 仍然非空，所以员工端「家政员订单 / 服务订单」的分类不受影响。
     *
     * @return 影响行数（0 表示这是服务订单，明细无需变更）
     */
    @Update("UPDATE order_detail SET housekeeper_id = #{housekeeperId}, name = #{name} " +
            "WHERE order_id = #{orderId} AND housekeeper_id IS NOT NULL")
    int updateHousekeeperInfo(@Param("orderId") Long orderId,
                              @Param("housekeeperId") Long housekeeperId,
                              @Param("name") String name);
}
