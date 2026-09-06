package com.smart.service;

import com.smart.dto.CategoryDTO;
import com.smart.dto.CategoryPageQueryDTO;
import com.smart.entity.Category;
import com.smart.result.PageResult;

import java.util.List;

public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据ID查询分类信息
     * @param id
     * @return
     */
    Category getById(Long id);

    /**
     * 修改分类信息
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 查询全部分类（下拉列表用）
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
