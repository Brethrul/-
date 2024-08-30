package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {
    /**
     * 新增菜品 保存菜品信息和口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
