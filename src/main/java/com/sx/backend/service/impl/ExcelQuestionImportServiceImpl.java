package com.sx.backend.service.impl;

import com.sx.backend.entity.Question;
import com.sx.backend.entity.QuestionType;
import com.sx.backend.entity.DifficultyLevel;
import com.sx.backend.service.ExcelQuestionImportService;
import com.sx.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ExcelQuestionImportServiceImpl implements ExcelQuestionImportService {
    @Autowired
    private QuestionService questionService;

    @Override
    public int importQuestionsFromExcel(String bankId, MultipartFile file) {
        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Question> questions = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 假设第一行为表头
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Question q = new Question();
                q.setBankId(bankId);
                q.setContent(row.getCell(0).getStringCellValue());
                // 题型映射（中文转枚举）
                String typeStr = row.getCell(1).getStringCellValue();
                q.setType(mapTypeFromChinese(typeStr));
                // 选项处理
                String optionsStr = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;
                if (optionsStr != null && !optionsStr.trim().isEmpty()) {
                    q.setOptions(Arrays.asList(optionsStr.split(";")));
                } else {
                    q.setOptions(null);
                }
                q.setAnswer(row.getCell(3).getStringCellValue());
                q.setScore((float) row.getCell(4).getNumericCellValue());
                // 难度映射（中文转枚举）
                String diffStr = row.getCell(5).getStringCellValue();
                q.setDifficultylevel(mapDifficultyFromChinese(diffStr));
                // 其它字段同理
                questions.add(q);
            }
            count = questionService.batchAddQuestions(questions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // 工具方法：题型映射
    private QuestionType mapTypeFromChinese(String typeStr) {
        switch (typeStr) {
            case "单选": return QuestionType.SINGLE_CHOICE;
            case "多选": return QuestionType.MULTIPLE_CHOICE;
            case "填空": return QuestionType.FILL_BLANK;
            case "简答": return QuestionType.SHORT_ANSWER;
            case "编程": return QuestionType.PROGRAMMING;
            default: return null;
        }
    }
    // 工具方法：难度映射
    private DifficultyLevel mapDifficultyFromChinese(String diffStr) {
        switch (diffStr) {
            case "简单": return DifficultyLevel.EASY;
            case "中等": return DifficultyLevel.MEDIUM;
            case "困难": return DifficultyLevel.HARD;
            default: return null;
        }
    }
}
