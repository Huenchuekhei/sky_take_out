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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getTurnoverBymap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            //新增用户数量 select count(id) from user where create_time > ? and create_time < ?
            Integer totalUser = userMapper.getCountByUser(beginTime, endTime);
            //总用户数量 select count(id) from user where  create_time < ?
            Integer newUser = userMapper.getCountByUser(null, endTime);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> validcountList = new ArrayList<>();
        List<Integer> newcountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的有效订单数 select count(id) from orders where status = ? and order_time > ? and order_time < ?
            Integer validcount = orderMapper.getOrderCountByMap(Orders.COMPLETED, beginTime, endTime);
            //查询每天的订单数量 select count(id) from orders where order_time > ? and order_time < ?
            Integer newcount = orderMapper.getOrderCountByMap(null, beginTime, endTime);

            validcountList.add(validcount);
            newcountList.add(newcount);
        }
        //每天的有效订单总数
        Integer validcountSum = validcountList.stream().reduce(Integer::sum).get();
        //每天的订单总数
        Integer newcountSum = newcountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = validcountSum == 0 ? 0.0 : (validcountSum * 100.0) / newcountSum;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCompletionRate(orderCompletionRate)
                .validOrderCountList(StringUtils.join(validcountList, ","))
                .validOrderCount(validcountSum)
                .orderCountList(StringUtils.join(newcountList, ","))
                .totalOrderCount(newcountSum)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getTop10Sales(beginTime, endTime);
        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
