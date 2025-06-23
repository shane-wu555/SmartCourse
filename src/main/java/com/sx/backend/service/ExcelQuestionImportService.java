package com.sx.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelQuestionImportService {
    int importQuestionsFromExcel(String bankId, MultipartFile file);
}
