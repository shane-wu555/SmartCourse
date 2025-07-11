package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.TeacherStudentDTO;
import com.sx.backend.entity.CourseEnrollment;
import com.sx.backend.entity.Student;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.CourseEnrollmentMapper;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.util.ExcelUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherStudentServiceImplTest {

    @Mock
    private CourseEnrollmentMapper enrollmentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TeacherStudentServiceImpl teacherStudentService;

    private CourseDTO courseDTO;

    @BeforeEach
    void setup() {
        courseDTO = new CourseDTO();
        courseDTO.setCourseId("course123");
        courseDTO.setTeacherId("teacher123");
    }

    @Test
    void getStudentsByCourseId_shouldReturnStudents() {
        when(courseMapper.findById("course123")).thenReturn(courseDTO);
        List<TeacherStudentDTO> mockStudents = List.of(new TeacherStudentDTO());
        when(enrollmentMapper.findStudentsByCourseId("course123")).thenReturn(mockStudents);

        List<TeacherStudentDTO> result = teacherStudentService.getStudentsByCourseId("course123", "teacher123");
        assertEquals(1, result.size());
    }

    @Test
    void getStudentsByCourseId_shouldThrowIfCourseNotBelongsToTeacher() {
        courseDTO.setTeacherId("anotherTeacher");
        when(courseMapper.findById("course123")).thenReturn(courseDTO);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                teacherStudentService.getStudentsByCourseId("course123", "teacher123"));

        assertEquals(403, ex.getCode());
    }

    @Test
    void importStudentsToCourse_shouldImportSuccessfully() {
        try (MockedStatic<ExcelUtils> mocked = mockStatic(ExcelUtils.class)) {
            // 模拟返回的学号
            List<String> studentNumbers = List.of("202301", "202302");

            // mock 静态方法
            mocked.when(() -> ExcelUtils.parseStudentNumbers(multipartFile)).thenReturn(studentNumbers);

            // mock 课程、学生信息
            when(courseMapper.findById("course123")).thenReturn(courseDTO);
            when(studentMapper.findByStudentNumbers(studentNumbers)).thenReturn(List.of(
                    createStudent("uid1", "202301"),
                    createStudent("uid2", "202302")
            ));
            when(enrollmentMapper.existsByStudentIdAndCourseId(anyString(), anyString())).thenReturn(0);

            // 调用 service
            Map<String, Object> result = teacherStudentService.importStudentsToCourse("course123", "teacher123", multipartFile);

            // 验证结果
            assertEquals(2, result.get("successCount"));
            assertEquals(0, result.get("failCount"));
        }
    }

    @Test
    void importStudentsToCourse_shouldThrowIfFileParsingFails() {
        try (MockedStatic<ExcelUtils> mockedStatic = mockStatic(ExcelUtils.class)) {
            when(courseMapper.findById("course123")).thenReturn(courseDTO);

            // mock 静态方法抛出异常
            mockedStatic.when(() -> ExcelUtils.parseStudentNumbers(multipartFile))
                    .thenThrow(new IOException("测试模拟解析失败"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    teacherStudentService.importStudentsToCourse("course123", "teacher123", multipartFile));

            assertEquals(400, ex.getCode());
            assertEquals("文件解析失败", ex.getMessage());
        }
    }


    @Test
    void importStudentsToCourse_shouldThrowIfSomeStudentsNotFound() {
        try (MockedStatic<ExcelUtils> mocked = mockStatic(ExcelUtils.class)) {
            List<String> studentNumbers = List.of("202301", "202302");

            mocked.when(() -> ExcelUtils.parseStudentNumbers(multipartFile))
                    .thenReturn(studentNumbers);

            when(courseMapper.findById("course123")).thenReturn(courseDTO);
            when(studentMapper.findByStudentNumbers(studentNumbers))
                    .thenReturn(List.of(createStudent("uid1", "202301")));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    teacherStudentService.importStudentsToCourse("course123", "teacher123", multipartFile));

            assertTrue(ex.getMessage().contains("202302"));
        }
    }


    @Test
    void importStudentsToCourse_shouldSkipIfAlreadyEnrolled() {
        try (MockedStatic<ExcelUtils> mockedStatic = mockStatic(ExcelUtils.class)) {
            // 准备数据
            List<String> studentNumbers = List.of("202301");
            Student student = createStudent("uid1", "202301");

            // ✅ mock 静态方法 ExcelUtils.parseStudentNumbers()
            mockedStatic.when(() -> ExcelUtils.parseStudentNumbers(multipartFile))
                    .thenReturn(studentNumbers);

            when(courseMapper.findById("course123")).thenReturn(courseDTO);
            when(studentMapper.findByStudentNumbers(studentNumbers)).thenReturn(List.of(student));
            when(enrollmentMapper.existsByStudentIdAndCourseId("uid1", "course123")).thenReturn(1);

            Map<String, Object> result = teacherStudentService.importStudentsToCourse("course123", "teacher123", multipartFile);

            assertEquals(0, result.get("successCount"));
            assertEquals(1, result.get("failCount"));
            assertTrue(((List<String>) result.get("skipStudents")).contains("202301"));
        }
    }


    // 工具方法：快速构造学生对象
    private Student createStudent(String userId, String number) {
        Student student = new Student();
        student.setUserId(userId);
        student.setStudentNumber(number);
        return student;
    }
}
