package com.smart.service.impl;

import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.constant.StatusConstant;
import com.smart.dto.CategoryDTO;
import com.smart.dto.CategoryPageQueryDTO;
import com.smart.entity.Category;
import com.smart.exception.BaseException;
import com.smart.mapper.CategoryMapper;
import com.smart.result.PageResult;
import com.smart.service.CategoryService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        // 校验分类名称是否已存在
        Category existing = categoryMapper.getByName(categoryDTO.getName());
        if (existing != null) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        Category category = new Category();

        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.ENABLE);
        // createTime、updateTime、createUser、updateUser 由 AOP 自动填充，无需手动设置
        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 开始进行分页查询
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据ID查询分类信息
     *
     * @param id
     * @return
     */
    @Override
    public Category getById(Long id) {
        return categoryMapper.getById(id);
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        // 校验分类名称是否与其他分类重复（排除自身）
        Category existing = categoryMapper.getByName(categoryDTO.getName());
        if (existing != null && !existing.getId().equals(categoryDTO.getId())) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        // updateTime、updateUser 由 AOP 自动填充，无需手动设置
        categoryMapper.update(category);
    }

    /**
     * 启用禁用分类
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();
        // updateTime、updateUser 由 AOP 自动填充，无需手动设置
        categoryMapper.startOrStop(category);
    }

    /**
     * 删除分类
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        log.info("删除分类信息，分类ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        categoryMapper.delete(id);
        log.info("删除分类信息成功");
    }

    /**
     * 查询全部分类（下拉列表用）
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
