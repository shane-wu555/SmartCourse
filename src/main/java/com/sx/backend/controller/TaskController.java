package com.sx.backend.controller;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.entity.Task;
import com.sx.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/courses/{courseId}/tasks")
    public ResponseEntity<ApiResponse<Task>> createTask(@PathVariable String courseId,
                                                        @RequestBody TaskDTO taskDTO) {
        taskDTO.setCourseId(courseId);
        Task createdTask = taskService.createTask(taskDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "任务创建成功", createdTask));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable String taskId,
                                                        @RequestBody TaskDTO taskDTO) {
        Task updatedTask = taskService.updateTask(taskId, taskDTO);
        return ResponseEntity.ok(ApiResponse.success("任务更新成功", updatedTask));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(204, "任务删除成功", null));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Task>> getTaskDetails(@PathVariable String taskId) {
        Task task = taskService.getTaskDetails(taskId);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @GetMapping("/courses/{courseId}/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getCourseTasks(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (size > 50) size = 50;
        List<Task> tasks = taskService.getCourseTasks(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
}