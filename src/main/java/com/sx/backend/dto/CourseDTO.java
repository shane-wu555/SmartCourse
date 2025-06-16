package com.sx.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CourseDTO {
    private String courseId;
    private String courseCode;
    private String name;
    private String description;
    private float credit;
    private int hours;
    private String semester;
    private String teacherId;
    private String teacherName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int studentCount; // 选修学生数
    private int taskCount;    // 关联任务数
}
