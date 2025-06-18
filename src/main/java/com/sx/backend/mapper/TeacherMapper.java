package com.sx.backend.mapper;

import com.sx.backend.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeacherMapper {
    // 根据ID查询教师
    Teacher findById(String teacherId);
    // 查询教师真实姓名（用于DTO转换）
    String findRealNameById (String teacherId);
    // 查询教师是否存在
    boolean existsById(String teacherId);

    // 插入教师特有信息
    int insertTeacher(Teacher teacher);
}
