package com.sx.backend.dto.request;

import com.sx.backend.entity.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskUpdateRequest {
    // 可选字段（部分更新）
    private String title;

    private String description;

    private TaskType type;

    @Future(message = "截止时间必须是未来时间")
    private LocalDateTime deadline;

    @Min(value = 0, message = "分数不能小于0")
    private Float maxScore;

    // 特殊字段
    private List<String> resourceIds; // 全量替换资源

    private Boolean publishStatus; // 发布状态（新增）

}