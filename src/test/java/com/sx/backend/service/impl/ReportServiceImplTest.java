/*package com.sx.backend.service.impl;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.Course;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.util.ExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private StudentMapper studentMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private TaskGradeMapper taskGradeMapper;
    @Mock
    private ExcelExporter excelExporter;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void generateCourseReport_shouldReturnNullWhenNoGrades() {
        // Arrange
        String courseId = "C101";
        when(gradeMapper.findByCourseId(courseId)).thenReturn(Collections.emptyList());

        // Act
        AnalysisReportDTO result = reportService.generateCourseReport(courseId);

        // Assert
        assertNull(result);
    }

    @Test
    void generateCourseReport_shouldCalculateCorrectAverage() {
        // Arrange
        String courseId = "C101";
        String courseName = "Mathematics";
        List<Grade> grades = Arrays.asList(
                createGrade("G1", "S1", 80.0),
                createGrade("G2", "S2", 90.0)
        );

        when(gradeMapper.findByCourseId(courseId)).thenReturn(grades);
        when(courseMapper.findById(courseId)).thenReturn(new CourseDTO());

        // Act
        AnalysisReportDTO result = reportService.generateCourseReport(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(courseId, result.getCourseId());
        assertEquals(courseName, result.getCourseName());
        assertEquals(85.0, result.getClassAverage(), 0.001);
    }

    @Test
    void generateCourseReport_shouldIdentifyTopAndBottomStudents() {
        // Arrange
        String courseId = "C101";
        List<Grade> grades = Arrays.asList(
                createGrade("G1", "S1", 60.0),
                createGrade("G2", "S2", 70.0),
                createGrade("G3", "S3", 80.0),
                createGrade("G4", "S4", 90.0),
                createGrade("G5", "S5", 100.0),
                createGrade("G6", "S6", 50.0)
        );

        when(gradeMapper.findByCourseId(courseId)).thenReturn(grades);
        when(courseMapper.findById(courseId)).thenReturn;
        mockStudentNames();
        mockTaskGrades();

        // Act
        AnalysisReportDTO result = reportService.generateCourseReport(courseId);

        // Assert
        assertEquals(5, result.getTopPerformers().size());
        assertEquals("S5", result.getTopPerformers().get(0).getStudentId());
        assertEquals(100.0, result.getTopPerformers().get(0).getAverageGrade(), 0.001);

        assertEquals(5, result.getNeedImprovement().size());
        assertEquals("S6", result.getNeedImprovement().get(0).getStudentId());
        assertEquals(50.0, result.getNeedImprovement().get(0).getAverageGrade(), 0.001);
    }

    @Test
    void exportGradeReport_shouldExportWhenReportExists() throws Exception {
        // Arrange
        String courseId = "C101";
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(gradeMapper.findByCourseId(courseId)).thenReturn(
                Collections.singletonList(createGrade("G1", "S1", 85.0))
        );
        when(courseMapper.findById(courseId)).thenReturn(new Course(courseId, "Math"));

        // Act
        reportService.exportGradeReport(courseId, response);

        // Assert
        verify(excelExporter, times(1)).exportCourseReport(any(AnalysisReportDTO.class), eq(response));
    }

    @Test
    void exportGradeReport_shouldHandleNullReport() throws Exception {
        // Arrange
        String courseId = "C101";
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(gradeMapper.findByCourseId(courseId)).thenReturn(Collections.emptyList());

        // Act
        reportService.exportGradeReport(courseId, response);

        // Assert
        verify(excelExporter, times(1)).exportCourseReport(isNull(), eq(response));
    }

    @Test
    void convertToStudentPerformance_shouldCalculateCompletionRate() {
        // Arrange
        Grade grade = createGrade("G1", "S1", 85.0);
        when(studentMapper.selectById("S1")).thenReturn(new Student("S1", "John Doe"));
        when(taskGradeMapper.findByGradeId("G1")).thenReturn(Arrays.asList(
                createTaskGrade(0.8),
                createTaskGrade(0.9)
        ));

        // Act
        AnalysisReportDTO.StudentPerformance sp = reportService.convertToStudentPerformance(grade);

        // Assert
        assertEquals("S1", sp.getStudentId());
        assertEquals("John Doe", sp.getStudentName());
        assertEquals(85.0, sp.getAverageGrade(), 0.001);
        assertEquals(0.85, sp.getCompletionRate(), 0.001); // (0.8 + 0.9) / 2
    }

    // Helper methods
    private Grade createGrade(String gradeId, String studentId, double finalGrade) {
        Grade grade = new Grade();
        grade.setGradeId(gradeId);
        grade.setStudentId(studentId);
        grade.setCourseId("C101");
        grade.setFinalGrade(finalGrade);
        return grade;
    }

    private TaskGrade createTaskGrade(double completionRate) {
        TaskGrade tg = new TaskGrade();
        tg.setCompletionRate(completionRate);
        return tg;
    }

    private void mockStudentNames() {
        when(studentMapper.selectById("S1")).thenReturn(new Student("S1", "Alice"));
        when(studentMapper.selectById("S2")).thenReturn(new Student("S2", "Bob"));
        when(studentMapper.selectById("S3")).thenReturn(new Student("S3", "Charlie"));
        when(studentMapper.selectById("S4")).thenReturn(new Student("S4", "David"));
        when(studentMapper.selectById("S5")).thenReturn(new Student("S5", "Eva"));
        when(studentMapper.selectById("S6")).thenReturn(new Student("S6", "Frank"));
    }

    private void mockTaskGrades() {
        when(taskGradeMapper.findByGradeId(any())).thenReturn(Arrays.asList(
                createTaskGrade(0.7),
                createTaskGrade(0.8)
        ));
    }
}*/
