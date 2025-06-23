// ExcelUtils.java
package com.sx.backend.util;

import com.sx.backend.dto.request.admin.AdminStudentCreateRequest;
import com.sx.backend.dto.request.admin.AdminTeacherCreateRequest;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {

    public static List<AdminStudentCreateRequest> parseStudentExcel(MultipartFile file) throws IOException {
        List<AdminStudentCreateRequest> students = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AdminStudentCreateRequest student = new AdminStudentCreateRequest();

                // 真实姓名 (第1列)
                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    student.setRealName(getCellValue(nameCell));
                }

                // 学号 (第2列)
                Cell numberCell = row.getCell(1);
                if (numberCell != null) {
                    student.setStudentNumber(getCellValue(numberCell));
                }

                // 年级 (第3列)
                Cell gradeCell = row.getCell(2);
                if (gradeCell != null) {
                    student.setGrade(getCellValue(gradeCell));
                }

                // 专业 (第4列)
                Cell majorCell = row.getCell(3);
                if (majorCell != null) {
                    student.setMajor(getCellValue(majorCell));
                }

                students.add(student);
            }
        }

        return students;
    }

    public static List<AdminTeacherCreateRequest> parseTeacherExcel(MultipartFile file) throws IOException {
        List<AdminTeacherCreateRequest> teachers = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AdminTeacherCreateRequest teacher = new AdminTeacherCreateRequest();

                // 真实姓名 (第1列)
                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    teacher.setRealName(getCellValue(nameCell));
                }

                // 工号 (第2列)
                Cell numberCell = row.getCell(1);
                if (numberCell != null) {
                    teacher.setEmployeeNumber(getCellValue(numberCell));
                }

                // 职称 (第3列)
                Cell titleCell = row.getCell(2);
                if (titleCell != null) {
                    teacher.setTitle(getCellValue(titleCell));
                }

                // 院系 (第4列)
                Cell deptCell = row.getCell(3);
                if (deptCell != null) {
                    teacher.setDepartment(getCellValue(deptCell));
                }

                // 简介 (第5列)
                Cell bioCell = row.getCell(4);
                if (bioCell != null) {
                    teacher.setBio(getCellValue(bioCell));
                }

                teachers.add(teacher);
            }
        }

        return teachers;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}