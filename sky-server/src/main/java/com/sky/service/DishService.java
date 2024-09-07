package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品 保存菜品信息和口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品的批量删除
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据菜品id查找菜品
     *
     * @param id
     * @return
     */
    DishVO getById(Long id);

    /**
     * 根据分类id查找菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 设置菜品的启售和停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void update(DishDTO dishDTO);
}
