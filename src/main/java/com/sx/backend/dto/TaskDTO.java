package com.sx.backend.dto;

import com.sx.backend.entity.TaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private String taskId;
    private String title;
    private String description;
    private TaskType type;
    private String typeDisplayName;
    private LocalDateTime deadline;
    private float maxScore;
    private List<ResourceDTO> resources;
}