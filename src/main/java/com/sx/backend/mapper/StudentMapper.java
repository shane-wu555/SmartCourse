package com.sx.backend.mapper;

import com.sx.backend.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper {
    // 插入学生信息
    int insertStudent(Student student);

}
