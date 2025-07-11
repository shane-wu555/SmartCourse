package com.sx.backend.service.impl;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.Resource;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskType;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.mapper.TestPaperMapper;
import com.sx.backend.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    private final TestPaperMapper testPaperMapper;

    private final SubmissionMapper submissionMapper;

    @Autowired
    public TaskServiceImpl(TaskMapper taskMapper, TestPaperMapper testPaperMapper, SubmissionMapper submissionMapper) {
        this.taskMapper = taskMapper;
        this.testPaperMapper = testPaperMapper;
        this.submissionMapper = submissionMapper;
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

        log.info("开始创建任务: {}", task);
        taskMapper.insert(task);
        log.info("任务已插入数据库，taskId={}", taskId);

        // ✅ 新增逻辑：绑定试卷并设置总分
        if (TaskType.EXAM_QUIZ.equals(taskDTO.getType()) && taskDTO.getPaperId() != null) {
            log.info("正在将试卷绑定到任务: paperId={}, taskId={}", taskDTO.getPaperId(), taskId);
            
            // 获取试卷信息
            TestPaper testPaper = testPaperMapper.selectById(taskDTO.getPaperId());
            if (testPaper != null && testPaper.getTotalScore() != null) {
                // 更新任务的总分为试卷的总分
                task.setMaxScore(testPaper.getTotalScore());
                taskMapper.update(task);
                log.info("已将任务总分设置为试卷总分: taskId={}, maxScore={}", taskId, testPaper.getTotalScore());
            } else {
                log.warn("试卷不存在或试卷总分为空: paperId={}", taskDTO.getPaperId());
            }
            
            testPaperMapper.updateTaskIdByPaperId(taskDTO.getPaperId(), taskId);
        }

        // 插入资源关联
        if (taskDTO.getResourceIds() != null && !taskDTO.getResourceIds().isEmpty()) {
            log.info("接收到的资源ID列表: {}", taskDTO.getResourceIds());
            int inserted = taskMapper.insertTaskResources(taskId, taskDTO.getResourceIds());
            log.info("成功插入 task_resource 关联 {} 条记录", inserted);
        } else {
            log.info("未收到任何资源ID，不插入 task_resource 记录");
        }

        // 插入知识点关联
        if (taskDTO.getPointIds() != null && !taskDTO.getPointIds().isEmpty()) {
            log.info("接收到的知识点ID列表: {}", taskDTO.getPointIds());
            int insertedPoints = taskMapper.insertTaskPoints(taskId, taskDTO.getPointIds());
            log.info("成功插入 task_knowledge_point 关联 {} 条记录", insertedPoints);
        } else {
            log.info("未收到任何知识点ID，不插入 task_knowledge_point 记录");
        }

        Task createdTask = taskMapper.getById(taskId);
        log.info("任务创建完成: {}", createdTask.getTaskId());
        return createdTask;
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
        List<Task> tasks = taskMapper.getByCourseId(courseId, offset, size);

        for (Task task : tasks) {
            task.setSubmissions(submissionMapper.findByTaskId(task.getTaskId()));
        }

        return tasks;
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
