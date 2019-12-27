package com.crui.service.order;

import com.crui.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {
    public List<CategoryReport> categoryReport(LocalDate date);
    public void createData();
    public List<Map> category1Count(String startDate, String endDate);
}
