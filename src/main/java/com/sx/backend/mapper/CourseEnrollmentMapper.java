// CourseEnrollmentMapper.java
package com.sx.backend.mapper;

import com.sx.backend.dto.TeacherStudentDTO;
import com.sx.backend.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseEnrollmentMapper {
    List<TeacherStudentDTO> findStudentsByCourseId(String courseId);
    int batchInsert(List<CourseEnrollment> enrollments);
    int existsByStudentIdAndCourseId(@Param("studentId") String studentId, @Param("courseId") String courseId);
}