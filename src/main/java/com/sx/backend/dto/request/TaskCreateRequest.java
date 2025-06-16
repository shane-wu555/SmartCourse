package com.sx.backend.dto.request;

import com.sx.backend.entity.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务创建请求DTO
 * 用于接收前端创建课程任务时传递的数据
 */
@Data
public class TaskCreateRequest {
    // 必填字段
    @NotBlank(message = "任务标题不能为空")
    private String title;

    @NotNull(message = "任务类型不能为空")
    private TaskType type;

    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须是未来时间")
    private LocalDateTime deadline;

    // 可选字段
    private String description;

    @Min(value = 0, message = "分数不能小于0")
    private Float maxScore = 0f;

    private List<String> resourceIds;

    private List<String> knowledgePointIds;
}
