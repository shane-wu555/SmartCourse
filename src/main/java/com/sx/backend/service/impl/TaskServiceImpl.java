package com.sx.backend.service.impl;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.Resource;
import com.sx.backend.entity.Task;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    @Autowired
    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public Task createTask(TaskDTO taskDTO) {
        if (taskDTO.getType() == null) {
            throw new BusinessException(400, "任务类型不能为空");
        }
        String taskId = UUID.randomUUID().toString();
        Task task = new Task(
                taskDTO.getCourseId(),
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getType(),
                taskDTO.getDeadline(),
                taskDTO.getMaxScore()
        );
        task.setTaskId(taskId);
        task.setCreatedAt(LocalDateTime.now());

        taskMapper.insert(task);

        if (taskDTO.getResourceIds() != null && !taskDTO.getResourceIds().isEmpty()) {
            taskMapper.insertTaskResources(taskId, taskDTO.getResourceIds());
        }

        if (taskDTO.getPointIds() != null && !taskDTO.getPointIds().isEmpty()) {
            taskMapper.insertTaskPoints(taskId, taskDTO.getPointIds());
        }

        return taskMapper.getById(taskId);
    }

    @Override
    @Transactional
    public Task updateTask(String taskId, TaskDTO taskDTO) {
        Task existingTask = taskMapper.getById(taskId);
        if (existingTask == null) {
            throw new BusinessException(404, "任务不存在");
        }

        // 只更新非空字段
        if (taskDTO.getTitle() != null) {
            existingTask.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            existingTask.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getType() != null) {
            existingTask.setType(taskDTO.getType());
        }
        if (taskDTO.getDeadline() != null) {
            existingTask.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getMaxScore() != null) {
            existingTask.setMaxScore(taskDTO.getMaxScore());
        }

        taskMapper.update(existingTask);

        // 更新关联关系
        if (taskDTO.getResourceIds() != null) {
            taskMapper.deleteTaskResources(taskId);
            if (!taskDTO.getResourceIds().isEmpty()) {
                taskMapper.insertTaskResources(taskId, taskDTO.getResourceIds());
            }
        }

        if (taskDTO.getPointIds() != null) {
            taskMapper.deleteTaskPoints(taskId);
            if (!taskDTO.getPointIds().isEmpty()) {
                taskMapper.insertTaskPoints(taskId, taskDTO.getPointIds());
            }
        }

        return taskMapper.getById(taskId);
    }

    @Override
    @Transactional
    public void deleteTask(String taskId) {
        Task task = taskMapper.getById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }

        int submissionCount = taskMapper.countSubmissions(taskId);
        if (submissionCount > 0) {
            throw new BusinessException(409, "任务已有学生提交，无法删除");
        }

        taskMapper.deleteTaskResources(taskId);
        taskMapper.deleteTaskPoints(taskId);
        taskMapper.delete(taskId);
    }

    @Override
    public List<Task> getCourseTasks(String courseId, int page, int size) {
        int offset = (page - 1) * size;
        return taskMapper.getByCourseId(courseId, offset, size);
    }

    @Override
    public Task getTaskDetails(String taskId) {
        Task task = taskMapper.getById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }

        // 加载关联资源
        List<Resource> resources = taskMapper.selectResourcesByTaskId(taskId);
        task.setResources(resources);

        // 加载关联知识点
        List<KnowledgePoint> knowledgePoints = taskMapper.selectPointsByTaskId(taskId);
        task.setKnowledgePoints(knowledgePoints);

        return task;
    }
}