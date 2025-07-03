package com.sx.backend.util;

import com.sx.backend.dto.AnalysisReportDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

@Component
public class ExcelExporter {

    // 列宽调整因子（解决中文字符宽度计算问题）
    private static final double COLUMN_WIDTH_FACTOR = 1.35;
    // 最大列宽（避免过宽）
    private static final int MAX_COLUMN_WIDTH = 10000;

    public void exportCourseReport(AnalysisReportDTO report, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            String sheetName = report.getCourseName() + "成绩分析";
            Sheet sheet = workbook.createSheet(truncateSheetName(sheetName, 31));

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle statStyle = createStatStyle(workbook);

            // 设置表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "得分率", "排名"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充学生数据
            int rowNum = 1;
            for (AnalysisReportDTO.StudentPerformance sp : report.getPerformers()) {
                Row row = sheet.createRow(rowNum++);
                fillStudentRow(row, sp, dataStyle);
            }

            // 添加统计信息
            Row statsRow = sheet.createRow(rowNum);

            Cell avgCell = statsRow.createCell(0);
            avgCell.setCellValue("班级平均分");
            avgCell.setCellStyle(statStyle);

            Cell avgValueCell = statsRow.createCell(1);
            avgValueCell.setCellValue(report.getClassAverage());
            avgValueCell.setCellStyle(statStyle);

            // 班级平均得分率
            Cell rateCell = statsRow.createCell(2);
            rateCell.setCellValue("班级平均得分率");
            rateCell.setCellStyle(statStyle);

            Cell rateValueCell = statsRow.createCell(3);
            rateValueCell.setCellValue(report.getClassAverageRate() + "%");
            rateValueCell.setCellStyle(statStyle);

            autoSizeColumns(sheet, headers.length);

            // 设置响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFileName = URLEncoder.encode(report.getCourseName() + "_成绩报表.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    private void fillStudentRow(Row row, AnalysisReportDTO.StudentPerformance sp, CellStyle style) {
        createCell(row, 0, sp.getStudentNumber(), style);
        createCell(row, 1, sp.getStudentName(), style);
        createCell(row, 2, sp.getGradeRate() + "%", style);
        createCell(row, 3, sp.getRank(), style);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);

            // 解决中文字符宽度计算问题
            int currentWidth = sheet.getColumnWidth(i);
            int adjustedWidth = (int) (currentWidth * COLUMN_WIDTH_FACTOR);

            // 设置最大宽度限制
            if (adjustedWidth > MAX_COLUMN_WIDTH) {
                sheet.setColumnWidth(i, MAX_COLUMN_WIDTH);
            } else {
                sheet.setColumnWidth(i, adjustedWidth);
            }
        }
    }

    // 截断工作表名称（Excel限制31字符）
    private String truncateSheetName(String name, int maxLength) {
        if (name.length() > maxLength) {
            return name.substring(0, maxLength - 3) + "...";
        }
        return name;
    }

    // 安全的单元格创建方法
    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        setCellValue(cell, value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    // 处理不同类型的数据
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // 创建表头样式
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    // 创建数据样式
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        setBorders(style);
        return style;
    }

    // 创建统计信息样式
    private CellStyle createStatStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        setBorders(style);
        return style;
    }

    // 设置单元格边框
    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
