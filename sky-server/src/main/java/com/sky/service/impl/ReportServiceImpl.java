package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.boot.starter.autoconfigure.SwaggerUiWebMvcConfigurer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    private SwaggerUiWebMvcConfigurer swaggerUiWebMvcConfigurer;
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        //如果begin和end是日期对象（如 Java 中的LocalDate），使用!=比较的是对象引用，而不是实际的日期值。
        // 即使两个对象代表相同的日期，只要它们是不同的对象实例，!=就会返回true。
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }


        List<Long> totalUserList = new ArrayList();
        List<Long> newUserList = new ArrayList();

        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();

            map.put("end", endTime);
            Long totalnum = orderMapper.countUser(map);
            map.put("begin", beginTime);
            Long newnum = orderMapper.countUser(map);

            if(totalnum==null){
                totalnum = Long.valueOf(0);
            }
            if(newnum==null){
                newnum = Long.valueOf(0);
            }

            totalUserList.add(totalnum);
            newUserList.add(newnum);
        }
        UserReportVO userReportVO = UserReportVO.builder().dateList(StringUtils.join(localDates, ",")).totalUserList(StringUtils.join(totalUserList,",")).newUserList(StringUtils.join(newUserList,",")).build();
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(begin);
        //如果begin和end是日期对象（如 Java 中的LocalDate），使用!=比较的是对象引用，而不是实际的日期值。
        // 即使两个对象代表相同的日期，只要它们是不同的对象实例，!=就会返回true。
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            localDates.add(begin);
        }
        List<Integer> ordertotalList = new ArrayList();
        List<Integer> ordervalidList = new ArrayList();
        Double orderCompletionRate;
        Integer ordertotalnum;
        Integer ordervalidnum;
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            map.put("begin", beginTime);
            map.put("status",null);
            ordertotalnum = orderMapper.countorder(map);
            map.put("status",Orders.COMPLETED);
            ordervalidnum = orderMapper.countorder(map);

            ordertotalList.add(ordertotalnum);
            ordervalidList.add(ordervalidnum);
        }
        ordertotalnum = ordertotalList.stream().reduce(0, Integer::sum);
        ordervalidnum = ordervalidList.stream().reduce(0, Integer::sum);
        if(ordertotalnum==0){
            orderCompletionRate = 0.0;
        }else{
            orderCompletionRate = ordervalidnum.doubleValue() / ordertotalnum;
        }
        OrderReportVO.OrderReportVOBuilder orderReportVO = OrderReportVO.builder().dateList(StringUtils.join(localDates, ","))
                .orderCountList(StringUtils.join(ordertotalList, ","))
                .validOrderCountList(StringUtils.join(ordervalidList, ","))
                .totalOrderCount(ordertotalnum)
                .validOrderCount(ordervalidnum)
                .orderCompletionRate(orderCompletionRate);

        return orderReportVO.build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String name = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String number = StringUtils.join(numbers, ",");

        return new SalesTop10ReportVO(name, number);
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库，获取营业数据
        LocalDate dayBegin = LocalDate.now().minusDays(30);
        LocalDate dayEnd = LocalDate.now().minusDays(1);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dayBegin, LocalTime.MIN), LocalDateTime.of(dayEnd, LocalTime.MAX));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        //通过POI将数据写入到EXCEL
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dayBegin + "至" + dayEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dayBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
