package com.sx.backend.service.impl;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.*;
import com.sx.backend.service.ReportService;
import com.sx.backend.util.ExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private ExcelExporter excelExporter;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    // 生成课程分析报告
    @Override
    public AnalysisReportDTO generateCourseReport(String courseId) {
        List<Grade> grades = gradeMapper.findByCourseId(courseId);
        if (grades.isEmpty()) return null;

        AnalysisReportDTO report = new AnalysisReportDTO();
        report.setCourseId(courseId);
        report.setCourseName(courseMapper.findById(courseId).getName());

        // 计算班级平均分
        double total = 0;
        for (Grade grade : grades) {
            total += grade.getFinalGrade();
        }
        report.setClassAverage(total / grades.size());

        report.setClassAverageRate(total / grades.size() / taskMapper.findTotalScoreByCourseId(courseId) * 100);

        // 按成绩排序
        grades.sort(Comparator.comparing(Grade::getFinalGrade).reversed());

        // 设置班级完成情况
        report.setPerformers(grades.stream()
                .limit(grades.size())
                .map(this::convertToStudentPerformance)
                .collect(Collectors.toList()));

        return report;
    }

    // 导出成绩报告
    @Override
    public void exportGradeReport(String courseId, HttpServletResponse response) {
        AnalysisReportDTO report = generateCourseReport(courseId);
        excelExporter.exportCourseReport(report, response);
    }

    // 将Grade转换为StudentPerformance DTO
    private AnalysisReportDTO.StudentPerformance convertToStudentPerformance(Grade grade) {
        AnalysisReportDTO.StudentPerformance sp = new AnalysisReportDTO.StudentPerformance();
        sp.setStudentNumber(studentMapper.selectById(grade.getStudentId()).getStudentNumber());
        sp.setStudentName(studentMapper.selectById(grade.getStudentId()).getRealName());
        float totalScore = taskMapper.findTotalScoreByCourseId(grade.getCourseId());
        sp.setGradeRate(grade.getFinalGrade() / totalScore * 100);
        sp.setRank(grade.getRankInClass());
        return sp;
    }
}
