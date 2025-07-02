package com.sx.backend.controller;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.service.QuestionService;
import com.sx.backend.service.TestPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//TODO 接口暂未进行验证
@RestController
@RequestMapping("/api/paper")
public class TestPaperController {
    @Autowired
    private TestPaperService testPaperService;
    
    @Autowired
    private QuestionService questionService;

    /**
     * 智能组卷接口
     * @param requestDTO 组卷请求参数
     * @return 生成的试卷对象
     */
    @PostMapping("/generate")
    public ApiResponse<TestPaper> generatePaper(@RequestBody GeneratePaperRequestDTO requestDTO) {
        try {
            // 参数验证
            if (requestDTO.getBankId() == null || requestDTO.getBankId().trim().isEmpty()) {
                return ApiResponse.error("题库ID不能为空，请先选择题库");
            }
            if (requestDTO.getTotalCount() == null || requestDTO.getTotalCount() <= 0) {
                return ApiResponse.error("题目总数必须大于0");
            }
            if (requestDTO.getMode() == null || requestDTO.getMode().trim().isEmpty()) {
                return ApiResponse.error("组卷方式不能为空");
            }
            
            TestPaper testPaper = testPaperService.generatePaper(requestDTO);
            return ApiResponse.success(testPaper);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("生成试卷失败：" + e.getMessage());
        }
    }

    /**
     * 根据试卷ID获取试卷详情
     * @param paperId 试卷ID
     * @return 试卷详情
     */
    @GetMapping("/{paperId}")
    public ApiResponse<TestPaper> getPaperById(@PathVariable String paperId) {
        try {
            TestPaper testPaper = testPaperService.getPaperById(paperId);
            if (testPaper == null) {
                return ApiResponse.error("试卷不存在");
            }
            return ApiResponse.success(testPaper);
        } catch (Exception e) {
            return ApiResponse.error("获取试卷失败：" + e.getMessage());
        }
    }

    /**
     * 根据课程ID获取试卷列表
     * @param courseId 课程ID
     * @return 试卷列表
     */
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<TestPaper>> getPapersByCourseId(@PathVariable String courseId) {
        try {
            List<TestPaper> papers = testPaperService.getPapersByCourseId(courseId);
            return ApiResponse.success(papers);
        } catch (Exception e) {
            return ApiResponse.error("获取试卷列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据任务ID获取试卷
     * @param taskId 任务ID
     * @return 试卷详情
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<TestPaper> getPaperByTaskId(@PathVariable String taskId) {
        try {
            TestPaper testPaper = testPaperService.getPaperByTaskId(taskId);
            if (testPaper == null) {
                return ApiResponse.error("该任务没有关联的试卷");
            }
            return ApiResponse.success(testPaper);
        } catch (Exception e) {
            return ApiResponse.error("获取试卷失败：" + e.getMessage());
        }
    }

    /**
     * 获取试卷的详细题目内容
     * @param paperId 试卷ID
     * @return 试卷详情及题目内容
     */
    @GetMapping("/{paperId}/questions")
    public ApiResponse<Map<String, Object>> getPaperWithQuestions(@PathVariable String paperId) {
        try {
            Map<String, Object> result = testPaperService.getPaperWithQuestions(paperId);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取试卷详情失败：" + e.getMessage());
        }
    }

    /**
     * 批量获取题目详情
     * @param questionIds 题目ID列表
     * @return 题目详情列表
     */
    @PostMapping("/questions/batch")
    public ApiResponse<List<Question>> getQuestionsByIds(@RequestBody List<String> questionIds) {
        try {
            if (questionIds == null || questionIds.isEmpty()) {
                return ApiResponse.error("题目ID列表不能为空");
            }
            
            List<Question> questions = questionService.getQuestionsByIds(questionIds);
            return ApiResponse.success(questions);
        } catch (Exception e) {
            return ApiResponse.error("获取题目详情失败：" + e.getMessage());
        }
    }
}
