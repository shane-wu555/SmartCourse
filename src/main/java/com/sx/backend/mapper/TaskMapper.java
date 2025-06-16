package com.sx.backend.mapper;

import com.sx.backend.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    int insert(Task task);
    int update(Task task);
    int delete(String taskId);
    Task findById(String taskId);
    List<Task> findByCourseId(String courseId);
    int LinkResourceToTask(@Param("taskId") String taskId, @Param("resourceId") String resourceId);
}
