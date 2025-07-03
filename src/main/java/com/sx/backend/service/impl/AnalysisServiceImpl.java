package com.sx.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.GradeTrendDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
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

        taskGrades.stream()
                .sorted(Comparator.comparing(TaskGrade::getGradedTime))
                .forEach(tg -> {
                    dates.add(tg.getGradedTime().format(DateTimeFormatter.ISO_DATE));
                    scores.add(tg.getScore());
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

            return dto;
        }catch (Exception e) {}
            return null;
    }
}
