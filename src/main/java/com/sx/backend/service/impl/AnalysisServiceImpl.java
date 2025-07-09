package com.sx.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.GradeTrendDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.mapper.TestPaperMapper;
import com.sx.backend.service.AnalysisService;
import com.sx.backend.util.ChartGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TestPaperMapper testPaperMapper;

    @Autowired
    private ChartGenerator chartGenerator;

    @Override
    public void updateGradeTrend(String studentId, String courseId) {
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) return;

        // 获取所有任务成绩按时间排序
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(studentId, courseId);

        ObjectNode trendData = new ObjectMapper().createObjectNode();
        ArrayNode dates = trendData.putArray("dates");
        ArrayNode scores = trendData.putArray("scores");
        ArrayNode totalScores = trendData.putArray("totalScores"); // 新增总分数组
        ArrayNode taskNames = trendData.putArray("taskNames"); // 新增任务名称数组

        taskGrades.stream()
                .sorted(Comparator.comparing(TaskGrade::getGradedTime))
                .forEach(tg -> {
                    dates.add(tg.getGradedTime().format(DateTimeFormatter.ISO_DATE));
                    scores.add(tg.getScore());
                    
                    // 获取该任务的总分和任务名称
                    Task task = taskMapper.getById(tg.getTaskId());
                    Float taskMaxScore = 100.0f; // 默认值
                    String taskName = "未知任务"; // 默认任务名称
                    
                    System.out.println("DEBUG: 处理任务 " + tg.getTaskId() + ", task对象: " + (task != null ? "存在" : "不存在"));
                    
                    if (task != null) {
                        // 获取任务名称
                        taskName = task.getTitle() != null ? task.getTitle() : "任务" + tg.getTaskId();
                        
                        System.out.println("DEBUG: task.getMaxScore() = " + task.getMaxScore());
                        System.out.println("DEBUG: task.getTestPaperId() = " + task.getTestPaperId());
                        System.out.println("DEBUG: task.getTitle() = " + task.getTitle());
                        
                        if (task.getMaxScore() != null && task.getMaxScore() > 0) {
                            taskMaxScore = task.getMaxScore();
                            System.out.println("DEBUG: 使用任务的maxScore: " + taskMaxScore);
                        } else if (task.getTestPaperId() != null) {
                            // 尝试通过试卷计算总分
                            try {
                                TestPaper testPaper = testPaperMapper.selectById(task.getTestPaperId());
                                System.out.println("DEBUG: 试卷对象: " + (testPaper != null ? "存在" : "不存在"));
                                
                                if (testPaper != null) {
                                    System.out.println("DEBUG: testPaper.getTotalScore() = " + testPaper.getTotalScore());
                                    System.out.println("DEBUG: testPaper.getQuestions() = " + (testPaper.getQuestions() != null ? testPaper.getQuestions().size() + "个题目" : "null"));
                                    
                                    if (testPaper.getTotalScore() != null && testPaper.getTotalScore() > 0) {
                                        taskMaxScore = testPaper.getTotalScore();
                                        System.out.println("DEBUG: 使用试卷的totalScore: " + taskMaxScore);
                                    } else if (testPaper.getQuestions() != null && !testPaper.getQuestions().isEmpty()) {
                                        // 通过题目分数计算总分
                                        taskMaxScore = testPaper.getQuestions().stream()
                                                .map(Question::getScore)
                                                .filter(score -> score != null && score > 0)
                                                .reduce(0.0f, Float::sum);
                                        System.out.println("DEBUG: 通过题目计算的总分: " + taskMaxScore);
                                        if (taskMaxScore == 0.0f) {
                                            taskMaxScore = 100.0f; // 如果还是0，使用默认值
                                            System.out.println("DEBUG: 题目总分为0，使用默认值100");
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: 无法从试卷获取总分: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("DEBUG: 任务没有maxScore和testPaperId，使用默认值100");
                        }
                    } else {
                        System.out.println("DEBUG: 任务不存在，使用默认值100");
                    }
                    
                    totalScores.add(taskMaxScore);
                    taskNames.add(taskName); // 添加任务名称
                    
                    // 调试信息
                    System.out.println("TaskGrade: " + tg.getTaskGradeId() + ", TaskId: " + tg.getTaskId() + 
                                     ", Score: " + tg.getScore() + ", MaxScore: " + taskMaxScore + ", TaskName: " + taskName);
                });

        // 生成趋势图
        List<String> date = new ArrayList<>();
        List<Float> score = new ArrayList<>();
        for (JsonNode node: dates) {
            date.add(node.asText());
        }
        for (JsonNode node: scores) {
            score.add((float)node.asDouble());
        }

        String chartImage = chartGenerator.generateLineChart(date, score);
        trendData.put("chartImage", chartImage);

        // 更新到成绩实体
        grade.setGradeTrend(trendData.toString());
        gradeMapper.update(grade);
        
        // 调试信息：打印生成的趋势数据
        System.out.println("Generated trend data: " + trendData.toString());
    }

    @Override
    public GradeTrendDTO getGradeTrend(String studentId, String courseId) {
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null || grade.getGradeTrend() == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode trend = objectMapper.readTree(grade.getGradeTrend());
            
            // 调试信息：打印解析的趋势数据
            System.out.println("Parsed trend data: " + trend.toString());
            
            GradeTrendDTO dto = new GradeTrendDTO();

            // 从JsonNode提取数据
            if (trend.has("dates")) {
                List<String> dates = new ArrayList<>();
                trend.get("dates").forEach(date -> dates.add(date.asText()));
                dto.setDates(dates);
            }

            if (trend.has("scores")) {
                List<Float> scores = new ArrayList<>();
                trend.get("scores").forEach(score -> scores.add(score.floatValue()));
                dto.setScores(scores);
            }

            // 新增：提取总分数据
            if (trend.has("totalScores")) {
                List<Float> totalScores = new ArrayList<>();
                trend.get("totalScores").forEach(totalScore -> totalScores.add(totalScore.floatValue()));
                dto.setTotalScores(totalScores);
                System.out.println("TotalScores extracted: " + totalScores);
            } else {
                System.out.println("No totalScores found in trend data!");
            }

            // 新增：提取任务名称数据
            if (trend.has("taskNames")) {
                List<String> taskNames = new ArrayList<>();
                trend.get("taskNames").forEach(taskName -> taskNames.add(taskName.asText()));
                dto.setTaskNames(taskNames);
                System.out.println("TaskNames extracted: " + taskNames);
            } else {
                System.out.println("No taskNames found in trend data!");
            }

            System.out.println("Final DTO: dates=" + dto.getDates() + ", scores=" + dto.getScores() + 
                             ", totalScores=" + dto.getTotalScores() + ", taskNames=" + dto.getTaskNames());
            return dto;
        }catch (Exception e) {}
            return null;
    }
}
