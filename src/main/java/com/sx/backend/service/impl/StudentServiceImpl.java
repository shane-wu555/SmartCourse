package com.sx.backend.service.impl;

import com.sx.backend.entity.Course;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.service.StudentService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public List<Course> getAllCourses(String studentId) {
        return studentMapper.selectAllCourses(studentId);
    }

    @Override
    public PageResult<Course> getCoursesByPage(String studentId, int page, int size) {
        int offset = (page - 1) * size;
        List<Course> courses = studentMapper.selectCoursesByPage(studentId, offset, size);
        long total = studentMapper.countCourses(studentId);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResult<>(page, size, total, totalPages, courses);
    }

    @Override
    public boolean enrollCourse(String studentId, String courseId) {
        // 检查是否已经选修
        if (studentMapper.isEnrolled(studentId, courseId) > 0) {
            throw new BusinessException(409, "您已选修该课程");
        }

        return studentMapper.enrollCourse(studentId, courseId) > 0;
    }

    @Override
    public boolean dropCourse(String studentId, String courseId) {
        return studentMapper.dropCourse(studentId, courseId) > 0;
    }

    @Override
    public PageResult<Course> searchCourses(String studentId, String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<Course> courses = studentMapper.searchCourses(studentId, keyword, offset, size);
        long total = studentMapper.countSearchCourses(studentId, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResult<>(page, size, total, totalPages, courses);
    }

    @Override
    public Course getCourseDetail(String studentId, String courseId) {
        Course course = studentMapper.selectCourseDetail(studentId, courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在或您未选修该课程");
        }
        return course;
    }
}