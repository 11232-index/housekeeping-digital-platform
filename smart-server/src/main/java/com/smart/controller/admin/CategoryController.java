package com.smart.controller.admin;

import com.smart.annotation.RoleCheck;
import com.smart.dto.CategoryDTO;
import com.smart.dto.CategoryPageQueryDTO;
import com.smart.entity.Category;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理（CRUD）
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @RoleCheck({"admin", "manager"})
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类，分类数据：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类信息分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询全部分类（下拉列表用）
     *
     * @param type
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        log.info("查询全部分类列表，类型：{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

    /**
     * 根据id查询分类信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        log.info("根据id查询分类信息：{}", id);
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @RoleCheck({"admin", "manager"})
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("分类信息修改：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @RoleCheck({"admin", "manager"})
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用分类：status={}, id={}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result delete(@PathVariable Long id) {
        log.info("分类信息删除：{}", id);
        categoryService.delete(id);
        return Result.success();
    }
}
