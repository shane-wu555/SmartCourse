package com.sx.backend.dto.request;

import com.sx.backend.entity.ManualGrade;
import com.sx.backend.util.Pair;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ManualGradingRequest {
    private List<ManualGrade> questionGrades; // 每个问题的分数和教师反馈
    private String feedback; // 总的教师反馈
}
