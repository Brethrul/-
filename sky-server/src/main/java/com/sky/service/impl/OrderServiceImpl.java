package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入1条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee());
        order.setUserId(userId);
        order.setAddress(addressBook.getProvinceName() + addressBook.getCityName() +
                addressBook.getDistrictName() + addressBook.getDetail());

        orderMapper.insert(order);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向订单明细表插入多条数据
        for(ShoppingCart sc:shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        //清空用户购物车
        shoppingCartMapper.delete(shoppingCart);
        //封装vo返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
        return orderSubmitVO;
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //生成空json，跳过支付流程
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public PageResult pageQuery4User(Integer pageNum, Integer pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();

        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        //查询获得page
        return getPageResult(ordersPageQueryDTO);
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        orderVO.setOrderDetailList(orderDetailList);
        List<String> dishes = new ArrayList<>();
        for(OrderDetail od : orderDetailList){
            dishes.add(od.getName());
        }
        orderVO.setOrderDishes(dishes.toString());
        return orderVO;
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        //更改订单状态
        Orders order = orderMapper.getById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Integer status = order.getStatus();

        if(status > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if(status.equals(Orders.TO_BE_CONFIRMED)){
            //2待接单，给用户退款
            order.setStatus(Orders.CANCELLED);
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("用户取消");
        orderMapper.update(order);
        //todo 通知商家
    }

    @Override
    public void repeat(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);

        for(OrderDetail orderDetail : orderDetailList){
            ShoppingCart shoppingCart = new ShoppingCart();

            if(orderDetail.getDishId() != null){
                shoppingCart.setDishId(orderDetail.getDishId());
                shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            }else{
                shoppingCart.setSetmealId(orderDetail.getSetmealId());
            }

            shoppingCart.setUserId(orderMapper.getById(orderDetail.getOrderId()).getUserId());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCart.setImage(orderDetail.getImage());
            shoppingCart.setName(orderDetail.getName());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(orderDetail.getNumber());

            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        return getPageResult(ordersPageQueryDTO);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = orderMapper.getById(ordersConfirmDTO.getId());
        if(!order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(toBeConfirmed);
        orderStatisticsVO.setToBeConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (orderMapper.getById(ordersRejectionDTO.getId()) == null || !orderMapper.getById(ordersRejectionDTO.getId()).getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        //用户已支付，退款
        Orders orders = new Orders();
        if(order.getPayStatus() == 1){
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders order = orderMapper.getById(id);
        if(order == null || !Objects.equals(order.getStatus(), Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);
        if(order == null || !Objects.equals(order.getStatus(), Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    private PageResult getPageResult(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if(page != null && page.getTotal() > 0){
            for(Orders order : page){
                Long orderId = order.getId();

                List<OrderDetail> orderDetailList = orderDetailMapper.getById(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                List<String> dishes = new ArrayList<>();
                for(OrderDetail od : orderDetailList){
                        dishes.add(od.getName());
                }
                orderVO.setOrderDishes(dishes.toString());
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),list);
    }

}
