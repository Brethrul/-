package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {
    /**
     * 购物车添加商品
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 返回购物车列表，注意用户id直接调用basecontext
     */
    List<ShoppingCart> list();

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    void delete(ShoppingCartDTO shoppingCartDTO);

    /**
     * 删除购物车中的全部商品
     */
    void deleteAll();
}
