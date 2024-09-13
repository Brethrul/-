package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param order
     */
    void insert(Orders order);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查询订单
     * @param id
     * @return
     */
    Orders getById(Long id);

    /**
     * 查询每个状态订单的数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 根据起止时间以及状态统计营业额
     * @param map
     */
//    @Select("select sum(amount) from orders where order_time > #{} and order_time < #{max} and status = #{5}")
    Double sumByMap(Map map);

    /**
     * 根据起止时间和状态统计订单数
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 根据起止时间统计销量Top10
     * @param map
     * @return
     */
    List<GoodsSalesDTO> top10ByMap(Map map);
}
