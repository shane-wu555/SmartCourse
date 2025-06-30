package com.sx.backend.mapper;

import com.sx.backend.entity.FileMeta;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMetaMapper {

    @Insert("INSERT INTO file_meta (file_id, file_name, file_size, file_type, upload_time) " +
            "VALUES (#{fileId}, #{fileName}, #{fileSize}, #{fileType}, #{uploadTime})")
    int insertFileMeta(FileMeta fileMeta);
}
