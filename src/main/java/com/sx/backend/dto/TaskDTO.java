package com.sx.backend.dto;

import com.sx.backend.entity.TaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private String taskId;
    private String courseId;
    private String title;
    private String description;
    private TaskType type;
    private LocalDateTime deadline;
    private Float maxScore;
    private List<String> resourceIds;
    private List<String> pointIds;

    public String paperId;
}
