package com.sx.backend.service.impl;

import com.sx.backend.dto.TaskDTO;
import com.sx.backend.entity.*;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.mapper.TestPaperMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TestPaperMapper testPaperMapper;

    @Mock
    private SubmissionMapper submissionMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    ArgumentCaptor<String> taskIdCaptor;

    private TaskDTO taskDTO;

    @BeforeEach
    void setup() {
        taskDTO = new TaskDTO();
        taskDTO.setCourseId("course1");
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Task description");
        taskDTO.setType(TaskType.EXAM_QUIZ);
        taskDTO.setDeadline(LocalDateTime.now().plusDays(7));
        taskDTO.setMaxScore(100f);
        taskDTO.setPaperId("paper1");
        taskDTO.setResourceIds(List.of("res1", "res2"));
        taskDTO.setPointIds(List.of("kp1", "kp2"));
    }

    @Test
    void createTask_shouldCreateTaskWithResourcesAndPoints() {
        // Arrange
        TestPaper testPaper = new TestPaper();
        testPaper.setTotalScore(120f);
        when(testPaperMapper.selectById("paper1")).thenReturn(testPaper);

        Task insertedTask = new Task();
        insertedTask.setTaskId("generated-task-id");
        when(taskMapper.getById(anyString())).thenReturn(insertedTask);

        // Act
        Task created = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(created);

        // 捕获 UUID 并断言其值不为空
        verify(testPaperMapper).updateTaskIdByPaperId(eq("paper1"), taskIdCaptor.capture());
        String capturedTaskId = taskIdCaptor.getValue();
        assertNotNull(capturedTaskId);
        assertFalse(capturedTaskId.isEmpty());

        verify(taskMapper).insertTaskResources(eq(capturedTaskId), eq(taskDTO.getResourceIds()));
        verify(taskMapper).insertTaskPoints(eq(capturedTaskId), eq(taskDTO.getPointIds()));
    }

    @Test
    void createTask_shouldThrowIfTypeIsNull() {
        taskDTO.setType(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.createTask(taskDTO));
        assertEquals(400, ex.getCode());
    }

    @Test
    void updateTask_shouldUpdateNonNullFields() {
        Task existing = new Task();
        existing.setTaskId("task1");
        when(taskMapper.getById("task1")).thenReturn(existing);

        Task updated = new Task();
        updated.setTaskId("task1");
        when(taskMapper.getById("task1")).thenReturn(updated);

        taskDTO.setTitle("Updated Title");

        Task result = taskService.updateTask("task1", taskDTO);
        assertEquals("task1", result.getTaskId());

        verify(taskMapper).update(any(Task.class));
        verify(taskMapper).deleteTaskResources("task1");
        verify(taskMapper).insertTaskResources("task1", taskDTO.getResourceIds());
    }

    @Test
    void deleteTask_shouldDeleteIfNoSubmissions() {
        Task task = new Task();
        task.setTaskId("task1");
        when(taskMapper.getById("task1")).thenReturn(task);
        when(taskMapper.countSubmissions("task1")).thenReturn(0);

        taskService.deleteTask("task1");

        verify(taskMapper).deleteTaskResources("task1");
        verify(taskMapper).deleteTaskPoints("task1");
        verify(taskMapper).delete("task1");
    }

    @Test
    void deleteTask_shouldThrowIfTaskNotFound() {
        when(taskMapper.getById("task1")).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.deleteTask("task1"));
        assertEquals(404, ex.getCode());
    }

    @Test
    void getCourseTasks_shouldReturnTasksWithSubmissions() {
        Task task = new Task();
        task.setTaskId("t1");

        when(taskMapper.getByCourseId("c1", 0, 10)).thenReturn(List.of(task));
        when(submissionMapper.findByTaskId("t1")).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getCourseTasks("c1", 1, 10);

        assertEquals(1, result.size());
        verify(submissionMapper).findByTaskId("t1");
    }

    @Test
    void getTaskDetails_shouldReturnTaskWithResourcesAndPoints() {
        Task task = new Task();
        task.setTaskId("t1");

        when(taskMapper.getById("t1")).thenReturn(task);
        when(taskMapper.selectResourcesByTaskId("t1")).thenReturn(List.of(new Resource()));
        when(taskMapper.selectPointsByTaskId("t1")).thenReturn(List.of(new KnowledgePoint()));

        Task result = taskService.getTaskDetails("t1");

        assertNotNull(result.getResources());
        assertNotNull(result.getKnowledgePoints());
    }
}
