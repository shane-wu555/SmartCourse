package com.sx.backend.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程创建请求DTO
 * 用于接收前端创建课程时传递的数据
 */
@Data
public class CourseCreateRequest {
    @NotBlank(message = "课程编号不能为空")
    @Size(min = 5, max = 20, message = "课程编号长度需在5-20字符之间")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(min = 2, max = 100, message = "课程名称长度需在2-100字符之间")
    private String name;

    @Size(max = 500, message = "描述不能超过500字符")
    private String description;

    @DecimalMin(value = "0.5", message = "学分不能小于0.5")
    @DecimalMax(value = "10.0", message = "学分不能大于10.0")
    private float credit;

    @Min(value = 1, message = "学时不能小于1")
    @Max(value = 200, message = "学时不能大于200")
    private int hours;

    @Size(min = 5, max = 20, message = "学期长度需在5-20字符之间")
    private String semester;
}