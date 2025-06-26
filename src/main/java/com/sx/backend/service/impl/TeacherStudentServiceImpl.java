// TeacherStudentServiceImpl.java
package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.TeacherStudentDTO;
import com.sx.backend.entity.Course;
import com.sx.backend.entity.CourseEnrollment;
import com.sx.backend.entity.Student;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.CourseEnrollmentMapper;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.service.TeacherStudentService;
import com.sx.backend.util.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherStudentServiceImpl implements TeacherStudentService {

    private final CourseEnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;

    @Autowired
    public TeacherStudentServiceImpl(CourseEnrollmentMapper enrollmentMapper,
                                     CourseMapper courseMapper,
                                     StudentMapper studentMapper) {
        this.enrollmentMapper = enrollmentMapper;
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
    }


    @Override
    public List<TeacherStudentDTO> getStudentsByCourseId(String courseId, String teacherId) {
        // 验证课程是否属于该教师
        CourseDTO courseDto = courseMapper.findById(courseId);
        if (courseDto == null || !teacherId.equals(courseDto.getTeacherId())) {
            throw new BusinessException(403, "无权访问此课程");
        }

        return enrollmentMapper.findStudentsByCourseId(courseId);
    }

    @Override
    @Transactional
    public Map<String, Object> importStudentsToCourse(String courseId, String teacherId, MultipartFile file) {
        // 验证课程是否属于该教师
        CourseDTO courseDto = courseMapper.findById(courseId);
        if (courseDto == null || !teacherId.equals(courseDto.getTeacherId())) {
            throw new BusinessException(403, "无权操作此课程");
        }

        // 解析Excel文件获取学号列表
        List<String> studentNumbers;
        try {
            studentNumbers = ExcelUtils.parseStudentNumbers(file);
        } catch (IOException e) {
            throw new BusinessException(400, "文件解析失败");
        }

        // 根据学号查询学生信息
        List<Student> students = studentMapper.findByStudentNumbers(studentNumbers);
        if (students.size() != studentNumbers.size()) {
            // 找出不存在的学生
            Set<String> foundNumbers = students.stream()
                    .map(Student::getStudentNumber)
                    .collect(Collectors.toSet());

            List<String> notFound = studentNumbers.stream()
                    .filter(num -> !foundNumbers.contains(num))
                    .collect(Collectors.toList());

            throw new BusinessException(400, "以下学号不存在: " + String.join(",", notFound));
        }

        // 过滤已选课的学生
        List<CourseEnrollment> toAdd = new ArrayList<>();
        int skipCount = 0;
        List<String> skipStudents = new ArrayList<>();

        for (Student student : students) {
            // 检查是否已选课
            if (enrollmentMapper.existsByStudentIdAndCourseId(student.getUserId(), courseId) > 0) {
                skipCount++;
                skipStudents.add(student.getStudentNumber());
                continue;
            }

            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setStudentId(student.getUserId());
            enrollment.setCourseId(courseId);
            toAdd.add(enrollment);
        }

        // 批量插入
        if (!toAdd.isEmpty()) {
            enrollmentMapper.batchInsert(toAdd);
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", toAdd.size());
        result.put("failCount", skipCount);
        result.put("skipStudents", skipStudents);
        return result;
    }
}