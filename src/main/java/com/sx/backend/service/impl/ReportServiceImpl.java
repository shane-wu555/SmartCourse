package com.sx.backend.service.impl;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
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
    private GradeMapper gradeMapper;

    @Autowired
    private ExcelExporter excelExporter;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    @Override
    public AnalysisReportDTO generateCourseReport(String courseId) {
        List<Grade> grades = gradeMapper.findByCourseId(courseId);
        if (grades.isEmpty()) return null;

        AnalysisReportDTO report = new AnalysisReportDTO();
        report.setCourseId(courseId);
        report.setCourseName(grades.get(0).getCourse().getName());

        // 计算班级平均分
        double total = 0;
        for (Grade grade : grades) {
            total += grade.getFinalGrade();
        }
        report.setClassAverage(total / grades.size());

        // 按成绩排序
        grades.sort(Comparator.comparing(Grade::getFinalGrade).reversed());

        // 获取前5名
        report.setTopPerformers(grades.stream()
                .limit(5)
                .map(this::convertToStudentPerformance)
                .collect(Collectors.toList()));

        // 获取后5名
        report.setNeedImprovement(grades.stream()
                .skip(Math.max(0, grades.size() - 5))
                .map(this::convertToStudentPerformance)
                .collect(Collectors.toList()));

        return report;
    }

    @Override
    public void exportGradeReport(String courseId, HttpServletResponse response) {
        AnalysisReportDTO report = generateCourseReport(courseId);
        excelExporter.exportCourseReport(report, response);
    }

    private AnalysisReportDTO.StudentPerformance convertToStudentPerformance(Grade grade) {
        AnalysisReportDTO.StudentPerformance sp = new AnalysisReportDTO.StudentPerformance();
        sp.setStudentId(grade.getStudent().getStudentNumber());
        sp.setStudentName(grade.getStudent().getRealName());
        sp.setAverageGrade(grade.getFinalGrade());

        // 计算完成率
        List<TaskGrade> taskGrades = taskGradeMapper.findByGradeId(grade.getGradeId());
        double completionRate = taskGrades.stream()
                .mapToDouble(TaskGrade::getCompletionRate)
                .average()
                .orElse(0);

        sp.setCompletionRate(completionRate);
        return sp;
    }
}
