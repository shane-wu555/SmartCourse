package com.sx.backend.controller;

import com.sx.backend.entity.Question;
import com.sx.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//TODO 相关接口测试并未验证
@RestController
@RequestMapping("/api/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping("/add")
    public int addQuestion(@RequestBody Question question) {
        // 注意：建议使用 POST /api/questionbank/{bankId}/question/add 来添加题目
        // 这个端点要求在请求体中包含 bankId
        return questionService.addQuestion(question);
    }

    @PutMapping("/update")
    public int updateQuestion(@RequestBody Question question) {
        return questionService.updateQuestion(question);
    }

    @DeleteMapping("/delete/{id}")
    public int deleteQuestion(@PathVariable("id") String questionId) {
        return questionService.deleteQuestion(questionId);
    }

    @GetMapping("/get/{id}")
    public Question getQuestionById(@PathVariable("id") String questionId) {
        return questionService.getQuestionById(questionId);
    }

    @GetMapping("/list/{bankId}")
    public List<Question> getQuestionsByBankId(@PathVariable("bankId") String bankId) {
        return questionService.getQuestionsByBankId(bankId);
    }

    @PostMapping("/search")
    public List<Question> getQuestionsByCondition(@RequestParam String type,
                                                  @RequestParam String difficultylevel,
                                                  @RequestBody List<String> knowledgePointIds) {
        return questionService.getQuestionsByCondition(type, difficultylevel, knowledgePointIds);
    }
}
