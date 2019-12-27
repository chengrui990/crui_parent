package com.crui.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.pojo.order.CategoryReport;
import com.crui.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    private CategoryReportService categoryReportService;

    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){
        LocalDate date = LocalDate.of(2019, 4, 15);
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
//        localDate.
//        String date = localDate.format(fmt);
        return categoryReportService.categoryReport(date);
    }

    @GetMapping("/category1Count")
    public List<Map> category1Count(String startDate, String endDate){
        return categoryReportService.category1Count(startDate, endDate);
    }
}
