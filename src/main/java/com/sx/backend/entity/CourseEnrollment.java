// CourseEnrollment.java (选课实体)
package com.sx.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseEnrollment {
    private String enrollmentId;
    private String studentId;
    private String courseId;
    private LocalDateTime enrollmentTime;
    private String status; // ENROLLED, COMPLETED, WITHDRAWN
    private Float finalGrade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 学生信息（用于关联查询）
    private String studentNumber;
    private String realName;
    private String grade;
    private String major;
}