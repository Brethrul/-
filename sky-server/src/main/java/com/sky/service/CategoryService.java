package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {

    /**
     * 创建分类
     * @param categoryDTO
     */
    void create(CategoryDTO categoryDTO);

    /**
     * 设置分类状态
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 分类列表查询
     * @param type
     * @return
     */
    public List<Category> listQuery(Integer type);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 修改分类
     * @param category
     */
    void update(CategoryDTO category);
}
