package com.sx.backend.mapper;

import com.sx.backend.entity.Resource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResourceMapper {
    @Insert("INSERT INTO resource (resource_id, course_id, name, type, url, size, description, uploader_id, upload_time, view_count, duration) " +
            "VALUES (#{resourceId}, #{courseId}, #{name}, #{type}, #{url}, #{size}, #{description}, #{uploaderId}, #{uploadTime}, #{viewCount}, #{duration})")
    int insertResource(Resource resource);

    @Select("SELECT resource_id, course_id, name, type, url, size, description, uploader_id, upload_time, view_count, duration " +
            "FROM resource WHERE resource_id = #{resourceId}")
    Resource getResourceById(String resourceId);

    @Select("SELECT * FROM resource " +
            "WHERE course_id = #{courseId} " +
            "AND (#{type} IS NULL OR type = #{type}) " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM task_resource WHERE task_resource.resource_id = resource.resource_id " +
            ") " +
            "LIMIT #{size} OFFSET #{offset}")
    List<Resource> getResourcesByCourseId(String courseId, String type, int offset, int size);

    @Select("SELECT COUNT(*) FROM resource WHERE course_id = #{courseId} " +
            "AND (#{type} IS NULL OR type = #{type})")
    int countResourcesByCourseId(String courseId, String type);

    @Update("UPDATE resource SET name = #{name}, description = #{description} " +
            "WHERE resource_id = #{resourceId}")
    int updateResource(Resource resource);

    @Delete("DELETE FROM resource WHERE resource_id = #{resourceId}")
    int deleteResource(String resourceId);

    @Select("SELECT COUNT(*) FROM task_resource WHERE resource_id = #{resourceId}")
    int countTaskReferences(String resourceId);

    @Select("SELECT r.* FROM resource r " +
            "INNER JOIN resource_knowledge_point rkp ON r.resource_id = rkp.resource_id " +
            "WHERE rkp.point_id = #{pointId}")
    List<Resource> getResourcesByKnowledgePointId(String pointId);
}