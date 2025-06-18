package com.sx.backend.mapper;

import com.sx.backend.entity.Resource;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper {
    @Insert("INSERT INTO resource (resource_id, course_id, name, type, url, size, description, uploader_id, upload_time, view_count) " +
            "VALUES (#{resourceId}, #{courseId}, #{name}, #{type}, #{url}, #{size}, #{description}, #{uploaderId}, #{uploadTime}, #{viewCount})")
    int insertResource(Resource resource);
}
