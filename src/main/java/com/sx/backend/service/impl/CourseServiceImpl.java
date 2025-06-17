package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.entity.Course;
import com.sx.backend.entity.Teacher;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public List<CourseDTO> getCoursesByTeacherId(String teacherId) {
        // 直接查询DTO列表，避免转换
        return courseMapper.findByTeacherId(teacherId);
    }

    @Override
    public PageResult<CourseDTO> getCoursesByPage(String teacherId, int page, int size, String semester, String keyword) {
        int offset = (page - 1) * size;
        List<CourseDTO> dtoList = courseMapper.findByTeacherIdWithPaging(teacherId, offset, size, semester, keyword);
        long total = courseMapper.countByTeacherIdWithPaging(teacherId, semester, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResult<>(page, size, total, totalPages, dtoList);
    }

    @Override
    public CourseDTO createCourse(CourseCreateRequest request, String teacherId) {
        // 验证课程编号唯一性
        if (courseMapper.countByCourseCode(request.getCourseCode(), null) > 0) {
            throw new BusinessException(409, "课程编号已存在");
        }

        Course course = new Course();
        course.setCourseId(UUID.randomUUID().toString());
        course.setCourseCode(request.getCourseCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setHours(request.getHours());
        course.setSemester(request.getSemester());
        course.setTeacherId(teacherId); // 直接设置teacherId

        courseMapper.insert(course);

        // 返回新创建的课程DTO
        return courseMapper.findById(course.getCourseId());
    }

    @Override
    public CourseDTO getCourseDetail(String courseId, String teacherId) {
        CourseDTO courseDTO = courseMapper.findById(courseId);
        if (courseDTO == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(courseDTO.getTeacherId())) {
            throw new BusinessException(403, "无权访问此课程");
        }
        return courseDTO;
    }

    @Override
    public CourseDTO updateCourse(String courseId, CourseUpdateRequest request, String teacherId) {
        CourseDTO existingCourse = courseMapper.findById(courseId);
        if (existingCourse == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(existingCourse.getTeacherId())) {
            throw new BusinessException(403, "无权修改此课程");
        }
        // 验证课程编号唯一性（排除自身）
        if (courseMapper.countByCourseCode(request.getCourseCode(), courseId) > 0) {
            throw new BusinessException(409, "课程编号已存在");
        }

        // 创建更新对象
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseCode(request.getCourseCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setHours(request.getHours());
        course.setSemester(request.getSemester());

        courseMapper.update(course);

        // 返回更新后的课程DTO
        return courseMapper.findById(courseId);
    }

    @Override
    public void deleteCourse(String courseId, String teacherId) {
        CourseDTO courseDTO = courseMapper.findById(courseId);
        if (courseDTO == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(courseDTO.getTeacherId())) {
            throw new BusinessException(403, "无权删除此课程");
        }
        // 检查关联数据
        if (courseMapper.countEnrollmentsByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联学生，无法删除");
        }
        if (courseMapper.countTasksByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联任务，无法删除");
        }
        if (courseMapper.countResourcesByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联资源，无法删除");
        }

        courseMapper.delete(courseId);
    }
}