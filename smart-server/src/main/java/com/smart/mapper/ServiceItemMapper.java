package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.ServiceItemPageQueryDTO;
import com.smart.entity.ServiceItem;
import com.smart.entity.ServiceRequirement;
import com.smart.enumeration.OperationType;
import com.smart.vo.ServiceItemVO;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface ServiceItemMapper {

    /**
     * 新增服务项目
     * @param serviceItem
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into service_item (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) values " +
            "(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceItem serviceItem);

    /**
     * 分页查询服务项目（联表 category 获取分类名称）
     * @param serviceItemPageQueryDTO
     * @return
     */
    Page<ServiceItemVO> pageQuery(ServiceItemPageQueryDTO serviceItemPageQueryDTO);

    /**
     * 根据id查询服务项目
     * @param id
     * @return
     */
    @Select("select * from service_item where id = #{id}")
    ServiceItem getById(Long id);

    /**
     * 修改服务项目信息（动态更新）
     * @param serviceItem
     */
    @AutoFill(OperationType.UPDATE)
    void update(ServiceItem serviceItem);

    /**
     * 启用禁用服务项目
     * @param serviceItem
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update service_item set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void startOrStop(ServiceItem serviceItem);

    /**
     * 根据id删除服务项目
     * @param id
     */
    @Delete("delete from service_item where id = #{id}")
    void delete(Long id);

    /**
     * 批量删除服务项目
     * @param ids
     */
    void deleteBatch(@Param("ids") List<Long> ids);

    // ──────── 服务要求相关 ────────

    /**
     * 新增服务要求
     * @param requirement
     */
    @Insert("insert into service_requirement (service_item_id, name, value) values (#{serviceItemId}, #{name}, #{value})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRequirement(ServiceRequirement requirement);

    /**
     * 根据服务项目id删除服务要求
     * @param serviceItemId
     */
    @Delete("delete from service_requirement where service_item_id = #{serviceItemId}")
    void deleteRequirementsByServiceItemId(Long serviceItemId);

    /**
     * 根据服务项目id查询服务要求列表
     * @param serviceItemId
     * @return
     */
    @Select("select * from service_requirement where service_item_id = #{serviceItemId}")
    List<ServiceRequirement> getRequirementsByServiceItemId(Long serviceItemId);
}
