package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    //处理超时订单的方法
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        //select * from orders where status = ? and order_time &lt; (当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        if(ordersList != null && ordersList.size() > 0){
            for (Orders order : ordersList){

                Orders build = Orders.builder()
                        .id(order.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("订单超时,自动取消")
                        .cancelTime(LocalDateTime.now())
                        .build();
                orderMapper.update(build);
            }
        }
    }

    /**
     * 处理一直处在派送中的订单
     */
    //todo 这里逻辑不是很好，后期优化一下
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDelivery(){
        log.info("每天处理一直处于派送中的订单：{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().minusMinutes(1));

        if(ordersList != null && ordersList.size() > 0){
            for (Orders order : ordersList){
                Orders build = Orders.builder()
                        .id(order.getId())
                        .status(Orders.COMPLETED)
                        .build();
                orderMapper.update(build);
            }
        }

    }
}
