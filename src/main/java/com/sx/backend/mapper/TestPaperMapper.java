package com.sx.backend.mapper;

import com.sx.backend.entity.TestPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 试卷 Mapper 接口
 */
@Mapper
public interface TestPaperMapper {

    /**
     * 插入试卷
     * @param testPaper 试卷对象
     * @return 插入的行数
     */
    int insert(TestPaper testPaper);

    /**
     * 根据ID查询试卷
     * @param paperId 试卷ID
     * @return 试卷对象
     */
    TestPaper selectById(@Param("paperId") String paperId);

    /**
     * 根据课程ID查询试卷列表
     * @param courseId 课程ID
     * @return 试卷列表
     */
    List<TestPaper> selectByCourseId(@Param("courseId") String courseId);

    /**
     * 根据任务ID查询试卷
     * @param taskId 任务ID
     * @return 试卷对象
     */
    TestPaper selectByTaskId(@Param("taskId") String taskId);

    /**
     * 更新试卷
     * @param testPaper 试卷对象
     * @return 更新的行数
     */
    int update(TestPaper testPaper);

    /**
     * 根据ID删除试卷
     * @param paperId 试卷ID
     * @return 删除的行数
     */
    int deleteById(@Param("paperId") String paperId);

    /**
     * 分页查询试卷列表
     * @param courseId 课程ID（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 试卷列表
     */
    List<TestPaper> selectByPage(@Param("courseId") String courseId,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    /**
     * 统计试卷数量
     * @param courseId 课程ID（可选）
     * @return 试卷数量
     */
    int countByCondition(@Param("courseId") String courseId);
    void updateTaskIdByPaperId(@Param("paperId") String paperId, @Param("taskId") String taskId);

}
