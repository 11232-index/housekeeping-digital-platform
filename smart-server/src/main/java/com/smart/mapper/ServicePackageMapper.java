package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.ServicePackagePageQueryDTO;
import com.smart.entity.ServicePackage;
import com.smart.entity.ServicePackageItem;
import com.smart.enumeration.OperationType;
import com.smart.vo.ServicePackageVO;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface ServicePackageMapper {

    /**
     * 新增服务套餐
     * @param servicePackage
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into service_package (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) values " +
            "(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServicePackage servicePackage);

    /**
     * 分页查询服务套餐（联表 category 获取分类名称）
     * @param servicePackagePageQueryDTO
     * @return
     */
    Page<ServicePackageVO> pageQuery(ServicePackagePageQueryDTO servicePackagePageQueryDTO);

    /**
     * 根据id查询服务套餐
     * @param id
     * @return
     */
    @Select("select * from service_package where id = #{id}")
    ServicePackage getById(Long id);

    /**
     * 修改服务套餐信息（动态更新）
     * @param servicePackage
     */
    @AutoFill(OperationType.UPDATE)
    void update(ServicePackage servicePackage);

    /**
     * 启用禁用服务套餐
     * @param servicePackage
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update service_package set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void startOrStop(ServicePackage servicePackage);

    /**
     * 根据id删除服务套餐
     * @param id
     */
    @Delete("delete from service_package where id = #{id}")
    void delete(Long id);

    /**
     * 批量删除服务套餐
     * @param ids
     */
    void deleteBatch(@Param("ids") List<Long> ids);

    // ──────── 服务套餐项目关系相关 ────────

    /**
     * 新增服务套餐项目关系
     * @param item
     */
    @Insert("insert into service_package_requirement (service_package_id, name, value) values (#{servicePackageId}, #{name}, #{value})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPackageItem(ServicePackageItem item);

    /**
     * 根据服务套餐id删除关联的套餐项目
     * @param servicePackageId
     */
    @Delete("delete from service_package_requirement where service_package_id = #{servicePackageId}")
    void deletePackageItemsByPackageId(Long servicePackageId);

    /**
     * 根据服务套餐id查询关联的套餐项目列表
     * @param servicePackageId
     * @return
     */
    @Select("select * from service_package_requirement where service_package_id = #{servicePackageId}")
    List<ServicePackageItem> getPackageItemsByPackageId(Long servicePackageId);
}
