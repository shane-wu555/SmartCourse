package com.sx.backend.util;

import com.sx.backend.dto.AnalysisReportDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelExporter {
    public void exportCourseReport(AnalysisReportDTO report, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(report.getCourseName() + "成绩分析");

            // 设置表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "平均成绩", "任务完成率", "排名"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }

            // 填充学生数据
            int rowNum = 1;
            for (AnalysisReportDTO.StudentPerformance sp : report.getPerformers()) {
                Row row = sheet.createRow(rowNum++);
                fillStudentRow(row, sp);
            }

            // 添加统计信息
            Row statsRow = sheet.createRow(rowNum++);
            statsRow.createCell(0).setCellValue("班级平均分");
            statsRow.createCell(1).setCellValue(report.getClassAverage());

            // 设置响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + report.getCourseName() + "_成绩报表.xlsx");

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    private void fillStudentRow(Row row, AnalysisReportDTO.StudentPerformance sp) {
        row.createCell(0).setCellValue(sp.getStudentId());
        row.createCell(1).setCellValue(sp.getStudentName());
        row.createCell(2).setCellValue(sp.getAverageGrade());
        row.createCell(4).setCellValue(sp.getRank());
    }
}
