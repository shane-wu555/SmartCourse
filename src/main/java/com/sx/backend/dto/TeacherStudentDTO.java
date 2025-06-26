// TeacherStudentDTO.java (教师查看学生DTO)
package com.sx.backend.dto;

import lombok.Data;

@Data
public class TeacherStudentDTO {
    private String enrollmentId;
    private String studentId;
    private String studentNumber;
    private String realName;
    private String grade;
    private String major;
    private Float finalGrade;
    private String status;
}