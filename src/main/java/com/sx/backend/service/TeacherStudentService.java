// TeacherStudentService.java
package com.sx.backend.service;

import com.sx.backend.dto.TeacherStudentDTO;
import com.sx.backend.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface TeacherStudentService {
    List<TeacherStudentDTO> getStudentsByCourseId(String courseId, String teacherId);
    Map<String, Object> importStudentsToCourse(String courseId, String teacherId, MultipartFile file);
}