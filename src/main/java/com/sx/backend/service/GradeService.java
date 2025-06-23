package com.sx.backend.service;

import com.sx.backend.entity.TaskGrade;

public interface GradeService {
    /**
     * 更新任务成绩
     * @param taskGrade 任务成绩实体
     */
    void updateTaskGrade(TaskGrade taskGrade);

    /**
     * 更新总成绩
     * @param taskGrade
     */
    void updateFinalGrade(TaskGrade taskGrade);
}
