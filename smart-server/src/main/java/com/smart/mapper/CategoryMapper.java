package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.CategoryPageQueryDTO;
import com.smart.entity.Category;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     * @param category
     */
    @AutoFill(OperationType.INSERT) // 插入操作自动填充 createTime、updateTime、createUser、updateUser
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) values " +
            "(#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @Select("select * from category where id = #{id}")
    Category getById(Long id);

    /**
     * 根据名称查询分类
     * @param name
     * @return
     */
    @Select("select * from category where name = #{name}")
    Category getByName(String name);

    /**
     * 修改分类信息
     * @param category
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    void update(Category category);

    /**
     * 启用禁用分类
     * @param category
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update category set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void startOrStop(Category category);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void delete(Long id);

    /**
     * 查询全部分类（下拉列表用）
     * @param type
     * @return
     */
    @Select("<script>" +
            "select * from category" +
            "<where>" +
            "<if test='type != null'> and type = #{type}</if>" +
            " and status = 1" +
            "</where>" +
            " order by sort asc" +
            "</script>")
    java.util.List<Category> list(Integer type);
}
