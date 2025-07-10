package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.Course;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Student;
import com.sx.backend.mapper.*;
import com.sx.backend.util.ExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private ExcelExporter excelExporter;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void generateCourseReport_NoGrades_ReturnsNull() {
        when(gradeMapper.findByCourseId("C001")).thenReturn(Collections.emptyList());

        AnalysisReportDTO result = reportService.generateCourseReport("C001");

        assertNull(result);
        verify(gradeMapper).findByCourseId("C001");
    }

    @Test
    void generateCourseReport_WithGrades_ReturnsReport() {
        // 准备测试数据
        String courseId = "CS101";
        CourseDTO course = new CourseDTO();
        course.setCourseId(courseId);
        course.setName("Computer Science");

        List<Grade> grades = Arrays.asList(
                createGrade("S1", 85.0f, courseId, 2),
                createGrade("S2", 92.0f, courseId, 1),
                createGrade("S3", 78.0f, courseId, 3)
        );

        // 设置Mock行为
        when(gradeMapper.findByCourseId(courseId)).thenReturn(grades);
        when(courseMapper.findById(courseId)).thenReturn(course);
        when(taskMapper.findTotalScoreByCourseId(courseId)).thenReturn(100.0f);
        when(studentMapper.selectById("S1")).thenReturn(createStudent("S1", "1001", "Alice"));
        when(studentMapper.selectById("S2")).thenReturn(createStudent("S2", "1002", "Bob"));
        when(studentMapper.selectById("S3")).thenReturn(createStudent("S3", "1003", "Charlie"));

        // 执行测试
        AnalysisReportDTO report = reportService.generateCourseReport(courseId);

        // 验证结果
        assertNotNull(report);
        assertEquals(courseId, report.getCourseId());
        assertEquals("Computer Science", report.getCourseName());
        assertEquals(85.0f, report.getClassAverage(), 0.01); // (85+92+78)/3 = 85
        assertEquals(85.0f, report.getClassAverageRate(), 0.01); // 85/100 * 100 = 85

        // 验证学生表现数据
        List<AnalysisReportDTO.StudentPerformance> performers = report.getPerformers();
        assertEquals(3, performers.size());

        // 验证排序（降序）
        assertEquals("Bob", performers.get(0).getStudentName());
        assertEquals("Alice", performers.get(1).getStudentName());
        assertEquals("Charlie", performers.get(2).getStudentName());

        // 验证成绩率计算
        assertEquals(92.0f, performers.get(0).getGradeRate(), 0.01);

        // 验证排名
        assertEquals(1, performers.get(0).getRank());
        assertEquals(2, performers.get(1).getRank());
        assertEquals(3, performers.get(2).getRank());
    }

    @Test
    void exportGradeReport_ValidData_CallsExporter() {
        // 准备测试数据
        String courseId = "CS101";
        CourseDTO course = new CourseDTO();
        course.setCourseId(courseId);
        course.setName("Test Course");

        Grade grade = createGrade("S1", 80.0f, courseId, 1);

        // 设置Mock行为
        when(gradeMapper.findByCourseId(courseId)).thenReturn(Collections.singletonList(grade));
        when(courseMapper.findById(courseId)).thenReturn(course);
        when(taskMapper.findTotalScoreByCourseId(courseId)).thenReturn(100.0f);
        when(studentMapper.selectById("S1")).thenReturn(createStudent("S1", "1001", "Test"));

        // 执行测试
        reportService.exportGradeReport(courseId, httpServletResponse);

        // 验证Excel导出器被调用
        verify(excelExporter).exportCourseReport(any(AnalysisReportDTO.class), eq(httpServletResponse));
    }

    @Test
    void exportGradeReport_NoData_CallsExporterWithNull() {
        when(gradeMapper.findByCourseId("C002")).thenReturn(Collections.emptyList());

        reportService.exportGradeReport("C002", httpServletResponse);

        // 验证即使没有数据也调用了导出器
        verify(excelExporter).exportCourseReport(isNull(), eq(httpServletResponse));
    }

    // 修复：添加排名参数
    private Grade createGrade(String studentId, float finalGrade, String courseId, int rank) {
        Grade grade = new Grade();
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
        grade.setFinalGrade(finalGrade);
        grade.setRankInClass(rank); // 关键修复：设置排名
        return grade;
    }

    private Student createStudent(String id, String number, String name) {
        Student student = new Student();
        student.setStudentId(id);
        student.setStudentNumber(number);
        student.setRealName(name);
        return student;
    }

    @Test
    void generateCourseReport_ZeroTotalScore_HandlesGracefully() {
        String courseId = "MATH101";
        CourseDTO course = new CourseDTO();
        course.setCourseId(courseId);
        course.setName("Mathematics");

        Grade grade = createGrade("S1", 80.0f, courseId, 1);

        when(gradeMapper.findByCourseId(courseId)).thenReturn(Collections.singletonList(grade));
        when(courseMapper.findById(courseId)).thenReturn(course);
        when(taskMapper.findTotalScoreByCourseId(courseId)).thenReturn(0.0f);
        when(studentMapper.selectById("S1")).thenReturn(createStudent("S1", "1001", "Test"));

        AnalysisReportDTO report = reportService.generateCourseReport(courseId);

        // 验证除以零的处理
        assertTrue(Float.isInfinite((float) report.getClassAverageRate()) || Float.isNaN((float) report.getClassAverageRate()));
    }

    @Test
    void generateCourseReport_MissingStudentInfo_HandlesGracefully() {
        String courseId = "PHY101";
        CourseDTO course = new CourseDTO();
        course.setCourseId(courseId);
        course.setName("Physics");

        Grade grade = createGrade("S1", 90.0f, courseId, 1);

        when(gradeMapper.findByCourseId(courseId)).thenReturn(Collections.singletonList(grade));
        when(courseMapper.findById(courseId)).thenReturn(course);
        when(taskMapper.findTotalScoreByCourseId(courseId)).thenReturn(100.0f);
        when(studentMapper.selectById("S1")).thenReturn(null); // 学生信息缺失

        AnalysisReportDTO report = reportService.generateCourseReport(courseId);

        // 验证学生姓名为空或默认值
        assertEquals("未知学生", report.getPerformers().get(0).getStudentName());
    }
}