package com.sx.backend.controller;

import com.sx.backend.entity.Task;
import com.sx.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentTaskController {

    private final TaskService taskService;

    @Autowired
    public StudentTaskController(TaskService taskService) {
        this.taskService = taskService;
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
