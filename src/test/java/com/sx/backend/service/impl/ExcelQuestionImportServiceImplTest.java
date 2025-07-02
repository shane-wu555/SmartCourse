package com.sx.backend.service.impl;

import com.sx.backend.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExcelQuestionImportServiceImplTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private ExcelQuestionImportServiceImpl importService;

    // 测试辅助方法：创建测试用的Excel文件
    private MultipartFile createTestExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"题目内容", "题型", "选项", "答案", "分值", "难度"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 创建数据行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Java是什么？");
            dataRow.createCell(1).setCellValue("单选");
            dataRow.createCell(2).setCellValue("A.编程语言;B.咖啡;C.岛屿");
            dataRow.createCell(3).setCellValue("A");
            dataRow.createCell(4).setCellValue(2.0);
            dataRow.createCell(5).setCellValue("简单");

            // 写入到字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile("test.xlsx", "test.xlsx", "application/vnd.ms-excel", out.toByteArray());
        }
    }

    @Test
    void importQuestionsFromExcel_ShouldReturnCount_WhenValidFile() throws Exception {
        // 准备
        String bankId = "bank123";
        MultipartFile file = createTestExcelFile();

        // 模拟批量添加返回成功数量
        when(questionService.batchAddQuestions(anyList())).thenReturn(1);

        // 执行
        int count = importService.importQuestionsFromExcel(bankId, file);

        // 验证
        assertEquals(1, count);
        verify(questionService, times(1)).batchAddQuestions(anyList());
    }

    @Test
    void importQuestionsFromExcel_ShouldHandleEmptyOptions() throws Exception {
        // 准备
        String bankId = "bank123";
        MultipartFile file = createTestExcelFile();

        // 获取Workbook修改第二行的选项为空
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        row.getCell(2).setCellValue(""); // 清空选项

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        MultipartFile modifiedFile = new MockMultipartFile("test.xlsx", out.toByteArray());

        // 模拟服务
        when(questionService.batchAddQuestions(anyList())).thenReturn(1);

        // 执行
        int count = importService.importQuestionsFromExcel(bankId, modifiedFile);

        // 验证
        assertEquals(1, count);
    }

    @Test
    void importQuestionsFromExcel_ShouldSkipEmptyRows() throws Exception {
        // 准备
        String bankId = "bank123";
        MultipartFile file = createTestExcelFile();

        // 获取Workbook添加空行
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        sheet.createRow(2); // 空行

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        MultipartFile modifiedFile = new MockMultipartFile("test.xlsx", out.toByteArray());

        // 模拟服务
        when(questionService.batchAddQuestions(anyList())).thenReturn(1);

        // 执行
        int count = importService.importQuestionsFromExcel(bankId, modifiedFile);

        // 验证：仍然只处理1条有效数据
        assertEquals(1, count);
    }

    @Test
    void importQuestionsFromExcel_ShouldHandleConversionErrors() throws Exception {
        // 准备
        String bankId = "bank123";
        MultipartFile file = createTestExcelFile();

        // 获取Workbook修改为无效类型
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        row.getCell(1).setCellValue("无效题型"); // 无效题型
        row.getCell(5).setCellValue("无效难度"); // 无效难度

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        MultipartFile modifiedFile = new MockMultipartFile("test.xlsx", out.toByteArray());

        // 模拟服务
        when(questionService.batchAddQuestions(anyList())).thenReturn(1);

        // 执行
        int count = importService.importQuestionsFromExcel(bankId, modifiedFile);

        // 验证：应该能处理但类型为null
        assertEquals(1, count);
        verify(questionService).batchAddQuestions(argThat(list ->
                list.get(0).getType() == null &&
                        list.get(0).getDifficultyLevel() == null
        ));
    }

    @Test
    void importQuestionsFromExcel_ShouldReturnZero_WhenEmptyFile() throws Exception {
        // 准备空文件
        MultipartFile emptyFile = new MockMultipartFile(
                "empty.xlsx",
                new byte[0]
        );

        // 执行
        int count = importService.importQuestionsFromExcel("bank123", emptyFile);

        // 验证
        assertEquals(0, count);
        verify(questionService, never()).batchAddQuestions(anyList());
    }

    @Test
    void importQuestionsFromExcel_ShouldHandleIOException() throws Exception {
        // 准备会抛出IO异常的模拟文件
        MultipartFile corruptFile = mock(MultipartFile.class);
        when(corruptFile.getInputStream()).thenThrow(new IOException("Test exception"));

        // 执行
        int count = importService.importQuestionsFromExcel("bank123", corruptFile);

        // 验证
        assertEquals(0, count);
        verify(questionService, never()).batchAddQuestions(anyList());
    }
}
