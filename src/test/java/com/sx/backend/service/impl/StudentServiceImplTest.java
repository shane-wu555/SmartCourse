package com.sx.backend.service.impl;

import com.sx.backend.entity.Course;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceImplTest {

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    @BeforeEach
    void printPageResultFields() {
        try {
            // 打印 PageResult 类的所有字段
            System.out.println("PageResult 类字段列表:");
            Field[] fields = PageResult.class.getDeclaredFields();
            for (Field field : fields) {
                System.out.println("- " + field.getName() + ": " + field.getType().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("无法访问 PageResult 字段: " + e.getMessage());
        }
    }

    // 改进的辅助方法：通过反射获取字段值
    private <T> T getField(Object object, String fieldName, Class<T> fieldType)
            throws NoSuchFieldException, IllegalAccessException {
        // 获取对象的所有字段（包括父类）
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return fieldType.cast(field.get(object));
            } catch (NoSuchFieldException e) {
                // 尝试在父类中查找
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("字段 '" + fieldName + "' 未找到");
    }

    @Test
    void getAllCourses_ShouldReturnCourseList() {
        // 准备测试数据
        List<Course> expectedCourses = Collections.singletonList(new Course());
        when(studentMapper.selectAllCourses(anyString())).thenReturn(expectedCourses);

        // 执行方法
        List<Course> result = studentService.getAllCourses("S001");

        // 验证结果
        assertEquals(expectedCourses, result);
        verify(studentMapper).selectAllCourses("S001");
    }

    @Test
    void enrollCourse_WhenNotEnrolled_ShouldReturnTrue() {
        // 模拟未选修且未退课
        when(studentMapper.isEnrolled(anyString(), anyString())).thenReturn(0);
        when(studentMapper.isWithdrawn(anyString(), anyString())).thenReturn(0);
        when(studentMapper.enrollCourse(anyString(), anyString())).thenReturn(1);

        // 执行并验证
        assertTrue(studentService.enrollCourse("S001", "C101"));
        verify(studentMapper).enrollCourse("S001", "C101");
    }

    @Test
    void enrollCourse_WhenAlreadyEnrolled_ShouldThrowConflict() {
        when(studentMapper.isEnrolled(anyString(), anyString())).thenReturn(1);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> studentService.enrollCourse("S001", "C101"));

        assertEquals(409, exception.getCode());
        assertEquals("您已选修该课程", exception.getMessage());
        verify(studentMapper, never()).enrollCourse(anyString(), anyString());
    }

    @Test
    void enrollCourse_WhenPreviouslyWithdrawn_ShouldThrowConflict() {
        when(studentMapper.isEnrolled(anyString(), anyString())).thenReturn(0);
        when(studentMapper.isWithdrawn(anyString(), anyString())).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> studentService.enrollCourse("S001", "C101"));

        assertEquals(409, exception.getCode());
        assertEquals("退课后不能重选", exception.getMessage());
        verify(studentMapper, never()).enrollCourse(anyString(), anyString());
    }

    @Test
    void dropCourse_ShouldReturnTrue() {
        when(studentMapper.dropCourse(anyString(), anyString())).thenReturn(1);
        assertTrue(studentService.dropCourse("S001", "C101"));
        verify(studentMapper).dropCourse("S001", "C101");
    }

    @Test
    void dropCourse_ShouldReturnFalse() {
        when(studentMapper.dropCourse(anyString(), anyString())).thenReturn(0);
        assertFalse(studentService.dropCourse("S001", "C101"));
    }

    @Test
    void getCourseDetail_ShouldReturnCourse() {
        Course expectedCourse = new Course();
        when(studentMapper.selectCourseDetail(anyString(), anyString())).thenReturn(expectedCourse);

        Course result = studentService.getCourseDetail("S001", "C101");
        assertEquals(expectedCourse, result);
    }

    @Test
    void getCourseDetail_WhenNotFound_ShouldThrowException() {
        when(studentMapper.selectCourseDetail(anyString(), anyString())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> studentService.getCourseDetail("S001", "C101"));

        assertEquals(404, exception.getCode());
        assertEquals("课程不存在或您未选修该课程", exception.getMessage());
    }
}