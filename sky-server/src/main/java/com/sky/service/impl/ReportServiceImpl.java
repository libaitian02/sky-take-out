package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        //如果begin和end是日期对象（如 Java 中的LocalDate），使用!=比较的是对象引用，而不是实际的日期值。
        // 即使两个对象代表相同的日期，只要它们是不同的对象实例，!=就会返回true。
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }


        List<BigDecimal> turnoverList = new ArrayList();
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            BigDecimal amount = orderMapper.count(beginTime, endTime, Orders.COMPLETED);
            if(amount==null){
                amount = BigDecimal.valueOf(0.0);
            }
            turnoverList.add(amount);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder().dateList(StringUtils.join(localDates, ",")).turnoverList(StringUtils.join(turnoverList,",")).build();
        return turnoverReportVO;
    }

}
