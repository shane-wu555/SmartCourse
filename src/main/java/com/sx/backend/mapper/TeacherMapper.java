package com.sx.backend.mapper;

import com.sx.backend.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherMapper {
    // 根据ID查询教师
    Teacher findById(String teacherId);
    // 查询教师真实姓名（用于DTO转换）
    String findRealNameById (String teacherId);
    // 查询教师是否存在
    boolean existsById(String teacherId);
}
