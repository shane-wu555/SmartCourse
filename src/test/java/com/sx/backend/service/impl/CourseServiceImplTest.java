package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.entity.Course;
import com.sx.backend.exception.AccessDeniedException;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourseServiceImpl courseService;

    // 测试工具方法
    private CourseDTO createSampleCourseDTO(String courseId, String teacherId) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(courseId);
        dto.setTeacherId(teacherId);
        return dto;
    }

    private CourseCreateRequest createSampleCreateRequest() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setCourseCode("CS101");
        request.setName("Computer Science");
        request.setDescription("Intro to CS");
        request.setCredit(3);
        request.setHours(48);
        request.setSemester("Fall 2023");
        return request;
    }

    private CourseUpdateRequest createSampleUpdateRequest() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setCourseCode("CS101-UPDATED");
        request.setName("Updated CS");
        request.setDescription("Updated description");
        request.setCredit(4);
        request.setHours(60);
        request.setSemester("Spring 2024");
        return request;
    }

    // 测试方法
    @Test
    void getCoursesByTeacherId_ShouldReturnList() {
        // 准备
        String teacherId = "t001";
        List<CourseDTO> expected = Collections.singletonList(createSampleCourseDTO("c1", teacherId));
        when(courseMapper.findByTeacherId(teacherId)).thenReturn(expected);

        // 执行
        List<CourseDTO> result = courseService.getCoursesByTeacherId(teacherId);

        // 验证
        assertEquals(expected, result);
        verify(courseMapper).findByTeacherId(teacherId);
    }

    @Test
    void createCourse_ShouldSucceed_WhenCourseCodeUnique() {
        // 准备
        CourseCreateRequest request = createSampleCreateRequest();
        String teacherId = "t001";

        when(courseMapper.countByCourseCode(request.getCourseCode(), null)).thenReturn(0);
        when(courseMapper.insert(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setCourseId("new_course_id");
            return 1;
        });
        when(courseMapper.findById("new_course_id")).thenReturn(createSampleCourseDTO("new_course_id", teacherId));

        // 执行
        CourseDTO result = courseService.createCourse(request, teacherId);

        // 验证
        assertNotNull(result);
        assertEquals("new_course_id", result.getCourseId());
        verify(courseMapper).countByCourseCode(request.getCourseCode(), null);
        verify(courseMapper).insert(any(Course.class));
    }

    @Test
    void createCourse_ShouldThrow_WhenCourseCodeExists() {
        // 准备
        CourseCreateRequest request = createSampleCreateRequest();
        String teacherId = "t001";
        when(courseMapper.countByCourseCode(request.getCourseCode(), null)).thenReturn(1);

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class,
                () -> courseService.createCourse(request, teacherId));
        assertEquals(409, exception.getCode());
        assertEquals("课程编号已存在", exception.getMessage());
    }

    @Test
    void getCourseDetail_ShouldReturnCourse_WhenAuthorized() {
        // 准备
        String courseId = "c1";
        String teacherId = "t001";
        CourseDTO expected = createSampleCourseDTO(courseId, teacherId);

        when(courseMapper.findById(courseId)).thenReturn(expected);

        // 执行
        CourseDTO result = courseService.getCourseDetail(courseId, teacherId);

        // 验证
        assertEquals(expected, result);
    }

    @Test
    void getCourseDetail_ShouldThrow_WhenUnauthorized() {
        // 准备
        String courseId = "c1";
        String unauthorizedTeacher = "t002";
        CourseDTO course = createSampleCourseDTO(courseId, "t001"); // 真实教师ID

        when(courseMapper.findById(courseId)).thenReturn(course);

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class,
                () -> courseService.getCourseDetail(courseId, unauthorizedTeacher));
        assertEquals(403, exception.getCode());
        assertEquals("无权访问此课程", exception.getMessage());
    }

    @Test
    void updateCourse_ShouldSucceed_WhenAuthorized() {
        // 准备
        String courseId = "c1";
        String teacherId = "t001";
        CourseUpdateRequest request = createSampleUpdateRequest();

        CourseDTO existing = createSampleCourseDTO(courseId, teacherId);
        when(courseMapper.findById(courseId)).thenReturn(existing);
        when(courseMapper.countByCourseCode(request.getCourseCode(), courseId)).thenReturn(0);
        when(courseMapper.update(any(Course.class))).thenReturn(1);
        when(courseMapper.findById(courseId)).thenReturn(existing); // 模拟更新后的查询

        // 执行
        CourseDTO result = courseService.updateCourse(courseId, request, teacherId);

        // 验证
        assertNotNull(result);
        verify(courseMapper).update(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldThrow_WhenHasEnrollments() {
        // 准备
        String courseId = "c1";
        String teacherId = "t001";
        CourseDTO course = createSampleCourseDTO(courseId, teacherId);

        when(courseMapper.findById(courseId)).thenReturn(course);
        when(courseMapper.countEnrollmentsByCourseId(courseId)).thenReturn(1);

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class,
                () -> courseService.deleteCourse(courseId, teacherId));
        assertEquals(409, exception.getCode());
        assertEquals("课程存在关联学生，无法删除", exception.getMessage());
    }

    @Test
    void getCurrentTeacherId_ShouldReturnId_FromUserDetails() {
        // 准备
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new org.springframework.security.core.userdetails.User("t001", "", Collections.emptyList()));

        // 执行
        String result = courseService.getCurrentTeacherId();

        // 验证
        assertEquals("t001", result);
    }

    @Test
    void getCurrentTeacherId_ShouldThrow_WhenUnauthenticated() {
        // 准备
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // 执行和验证
        assertThrows(AccessDeniedException.class, () -> courseService.getCurrentTeacherId());
    }

    @Test
    void getCourseEntityById_ShouldReturnEntity() {
        // 准备
        String courseId = "c1";
        CourseDTO dto = createSampleCourseDTO(courseId, "t001");
        when(courseMapper.findById(courseId)).thenReturn(dto);

        // 执行
        Course result = courseService.getCourseEntityById(courseId);

        // 验证
        assertNotNull(result);
        assertEquals(courseId, result.getCourseId());
        assertEquals(dto.getName(), result.getName());
    }

    // 分页测试示例
    @Test
    void getCoursesByPage_ShouldReturnPageResult() {
        // 准备
        String teacherId = "t001";
        int page = 1, size = 10;
        List<CourseDTO> dtos = Collections.singletonList(createSampleCourseDTO("c1", teacherId));
        when(courseMapper.findByTeacherIdWithPaging(teacherId, 0, size, null, null)).thenReturn(dtos);
        when(courseMapper.countByTeacherIdWithPaging(teacherId, null, null)).thenReturn(1);

        // 执行
        PageResult<CourseDTO> result = courseService.getCoursesByPage(teacherId, page, size, null, null);

        // 验证
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(1, result.getTotal());
        assertEquals(dtos, result.getList());
    }
}

// 需要添加的PageResult类（测试用）
class PageResult<T> {
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private List<T> list;

    public PageResult(int page, int size, long total, int totalPages, List<T> list) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.list = list;
    }

    // Getters
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
    public List<T> getList() { return list; }
}
