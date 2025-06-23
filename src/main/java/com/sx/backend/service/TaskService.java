package com.sx.backend.service;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.entity.Task;

import java.util.List;

public interface TaskService {
    Task createTask(TaskDTO taskDTO);
    Task updateTask(String taskId, TaskDTO taskDTO);
    void deleteTask(String taskId);
    Task getTaskDetails(String taskId);
    List<Task> getCourseTasks(String courseId, int page, int size);
}