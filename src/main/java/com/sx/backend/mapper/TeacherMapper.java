package com.sx.backend.mapper;

import com.sx.backend.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {
    // 根据ID查询教师
    Teacher findById(String teacherId);

    // 查询教师真实姓名（用于DTO转换）
    String findRealNameById(String teacherId);

    // 查询教师是否存在
    boolean existsById(String teacherId);

    // 插入教师特有信息
    int insertTeacher(Teacher teacher);

    boolean existsByEmployeeNumber(String employeeNumber);

    int updateTeacher(Teacher teacher);

    int deleteTeacher(String teacherId);

    List<Teacher> findTeachersByCondition(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countTeachersByCondition(
            @Param("keyword") String keyword,
            @Param("department") String department
    );
    // 新增按工号查询方法
    Teacher findByEmployeeNumber(String employeeNumber);
}
