package com.sx.backend.service;

import com.sx.backend.entity.Submission;
import com.sx.backend.entity.TaskGrade;

public interface GradeService {
    /**
     * 更新任务成绩
     * @param submission 提交记录
     */
    void updateTaskGrade(Submission submission);

    /**
     * 更新总成绩
     * @param taskGrade
     */
    void updateFinalGrade(TaskGrade taskGrade);
}
