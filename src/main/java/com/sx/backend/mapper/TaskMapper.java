package com.sx.backend.mapper;

import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.Resource;
import com.sx.backend.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    int insert(Task task);

    int update(Task task);

    int delete(String taskId);

    Task getById(String taskId);

    List<Task> getByCourseId(@Param("courseId") String courseId,
                             @Param("offset") int offset,
                             @Param("size") int size);

    int countSubmissions(String taskId);

    int insertTaskResources(@Param("taskId") String taskId,
                            @Param("resourceIds") List<String> resourceIds);

    int insertTaskPoints(@Param("taskId") String taskId,
                         @Param("pointIds") List<String> pointIds);

    int deleteTaskResources(String taskId);

    int deleteTaskPoints(String taskId);

    List<Resource> selectResourcesByTaskId(String taskId);

    List<KnowledgePoint> selectPointsByTaskId(String taskId);
}