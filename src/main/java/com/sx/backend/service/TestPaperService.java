package com.sx.backend.service;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.TestPaper;

import java.util.List;
import java.util.Map;

/**
 * 智能组卷服务接口
 */
public interface TestPaperService {
    /**
     * 根据请求参数生成试卷并直接保存到数据库
     * @param requestDTO 组卷请求参数
     * @return 生成并保存的试卷对象
     */
    TestPaper generatePaper(GeneratePaperRequestDTO requestDTO);

    /**
     * 保存试卷到数据库
     * @param testPaper 试卷对象
     * @return 保存的试卷对象
     */
    TestPaper savePaper(TestPaper testPaper);

    /**
     * 根据ID查询试卷
     * @param paperId 试卷ID
     * @return 试卷对象
     */
    TestPaper getPaperById(String paperId);

    /**
     * 根据课程ID查询试卷列表
     * @param courseId 课程ID
     * @return 试卷列表
     */
    List<TestPaper> getPapersByCourseId(String courseId);

    /**
     * 根据任务ID查询试卷
     * @param taskId 任务ID
     * @return 试卷对象
     */
    TestPaper getPaperByTaskId(String taskId);

    /**
     * 更新试卷
     * @param testPaper 试卷对象
     * @return 更新的试卷对象
     */
    TestPaper updatePaper(TestPaper testPaper);

    /**
     * 根据ID删除试卷
     * @param paperId 试卷ID
     * @return 是否删除成功
     */
    boolean deletePaper(String paperId);

    /**
     * 分页查询试卷列表
     * @param courseId 课程ID（可选）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 试卷列表
     */
    List<TestPaper> getPapersByPage(String courseId, int page, int size);

    /**
     * 统计试卷数量
     * @param courseId 课程ID（可选）
     * @return 试卷数量
     */
    int countPapers(String courseId);

    /**
     * 获取试卷详情（包含完整的题目信息）
     * @param paperId 试卷ID
     * @return 包含试卷信息和题目详情的Map
     */
    Map<String, Object> getPaperWithQuestions(String paperId);
}
