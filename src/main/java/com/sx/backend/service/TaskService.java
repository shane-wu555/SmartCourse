package com.sx.backend.service;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.dto.request.TaskCreateRequest;
import com.sx.backend.dto.request.TaskUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {
    TaskDTO createTask(String courseId, TaskCreateRequest request);
    TaskDTO updateTask(String taskId, TaskUpdateRequest request);
    void deleteTask(String taskId);
    List<TaskDTO> getTaskByCourse(String courseId);
    TaskDTO addTaskResources(String taskId, List<MultipartFile> flies);
}
