package com.sx.backend.controller;

import com.sx.backend.entity.QuestionBank;
import com.sx.backend.entity.Question;
import com.sx.backend.service.QuestionBankService;
import com.sx.backend.service.QuestionService;
import com.sx.backend.service.ExcelQuestionImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questionBank")
public class QuestionBankController {
    @Autowired
    private QuestionBankService questionBankService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ExcelQuestionImportService excelQuestionImportService;

    @PostMapping("/add")
    public int addQuestionBank(@RequestBody QuestionBank bank) {
        return questionBankService.addQuestionBank(bank);
    }

    @DeleteMapping("/delete/{id}")
    public int deleteQuestionBank(@PathVariable("id") String bankId) {
        return questionBankService.deleteQuestionBank(bankId);
    }

    @PutMapping("/update")
    public int updateQuestionBank(@RequestBody QuestionBank bank) {
        return questionBankService.updateQuestionBank(bank);
    }

    @GetMapping("/get/{id}")
    public QuestionBank getQuestionBankById(@PathVariable("id") String bankId) {
        return questionBankService.getQuestionBankById(bankId);
    }

    @GetMapping("/list")
    public List<QuestionBank> getAllQuestionBanks() {
        return questionBankService.getAllQuestionBanks();
    }

    // 题库内题目增删改查
    @PostMapping("/{bankId}/question/add")
    public int addQuestionToBank(@PathVariable String bankId, @RequestBody Question question) {
        question.setBankId(bankId);
        return questionService.addQuestion(question);
    }

    @DeleteMapping("/{bankId}/question/delete/{questionId}")
    public int deleteQuestionFromBank(@PathVariable String bankId, @PathVariable String questionId) {
        return questionService.deleteQuestion(questionId);
    }

    @PutMapping("/{bankId}/question/update")
    public int updateQuestionInBank(@PathVariable String bankId, @RequestBody Question question) {
        question.setBankId(bankId);
        return questionService.updateQuestion(question);
    }

    @GetMapping("/{bankId}/question/list")
    public List<Question> getQuestionsByBankId(@PathVariable String bankId) {
        return questionService.getQuestionsByBankId(bankId);
    }

    // 批量导入题目
    @PostMapping("/{bankId}/question/batchImport")
    public int batchImportQuestions(@PathVariable String bankId, @RequestBody List<Question> questions) {
        int count = 0;
        for (Question q : questions) {
            q.setBankId(bankId);
            count += questionService.addQuestion(q);
        }
        return count;
    }

    // 从Excel文件导入题目
    @PostMapping("/{bankId}/question/importExcel")
    public int importQuestionsFromExcel(@PathVariable String bankId, @RequestParam("file") MultipartFile file) {
        return excelQuestionImportService.importQuestionsFromExcel(bankId, file);
    }
}
