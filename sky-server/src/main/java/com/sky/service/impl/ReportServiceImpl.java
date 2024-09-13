package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        //存放begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end) ) {
            //日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        for (LocalDate date: dateList){
            Map map = new HashMap();
            map.put("begin",LocalDateTime.of(date, LocalTime.MIN));
            map.put("end",LocalDateTime.of(date, LocalTime.MAX));
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        turnoverReportVO.setDateList(StringUtils.join(dateList,","));
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end) ) {
            //日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //总用户
        List<Integer> totalUserList = new ArrayList<>();
        //新增用户
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date: dateList){
            Map map = new HashMap();
            map.put("end",LocalDateTime.of(date, LocalTime.MAX));
            Integer totalUser = userMapper.sumByMap(map);
            map.put("begin",LocalDateTime.of(date, LocalTime.MIN));
            Integer newUser = userMapper.sumByMap(map);
            totalUser = totalUser == null ? 0 : totalUser;
            newUser = newUser == null ? 0 : newUser;

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,",")).build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end) ) {
            //日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList){
            Map map = new HashMap();
            map.put("end",LocalDateTime.of(date, LocalTime.MAX));
            map.put("begin",LocalDateTime.of(date, LocalTime.MIN));
            orderCountList.add(orderMapper.countByMap(map));
            map.put("status",Orders.COMPLETED);
            validOrderCountList.add(orderMapper.countByMap(map));
        }
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer :: intValue).sum();
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer :: intValue).sum();
        Double orderCompletionRate = (double) (validOrderCount / totalOrderCount);

        return OrderReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        Map map = new HashMap();
        map.put("end",end);
        map.put("begin",begin);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.top10ByMap(map);

        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }
}
