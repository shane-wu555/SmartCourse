package com.sx.backend.mapper;

import com.sx.backend.entity.Course;
import com.sx.backend.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {
    // 根据ID查询学生
    Student selectById(String studentId);

    // 获取学生所有课程（不分页）
    List<Course> selectAllCourses(String studentId);

    // 分页获取学生课程
    List<Course> selectCoursesByPage(@Param("studentId") String studentId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    // 获取学生课程总数
    long countCourses(String studentId);

    // 选修课程
    int enrollCourse(@Param("studentId") String studentId,
                     @Param("courseId") String courseId);

    // 删除课程（退课）
    int dropCourse(@Param("studentId") String studentId,
                   @Param("courseId") String courseId);

    // 搜索课程（分页）
    List<Course> searchCourses(@Param("studentId") String studentId,
                               @Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("size") int size);

    // 获取搜索课程总数
    long countSearchCourses(@Param("studentId") String studentId,
                            @Param("keyword") String keyword);

    // 获取课程详情（需验证学生是否选修）
    Course selectCourseDetail(@Param("studentId") String studentId,
                              @Param("courseId") String courseId);

    // 判断学生是否已选修这门课
    int isEnrolled(@Param("studentId") String studentId,
                   @Param("courseId") String courseId);

    // 插入学生信息
    int insertStudent(Student student);

    int updateStudent(Student student);

    boolean existsByStudentNumber(String studentNumber);

    List<Student> findStudentsByCondition(
            @Param("keyword") String keyword,
            @Param("grade") String grade,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countStudentsByCondition(
            @Param("keyword") String keyword,
            @Param("grade") String grade
    );

    int deleteStudent(String studentId);

    // 添加新方法
    Student selectByStudentNumber(String studentNumber);

    int isWithdrawn(@Param("studentId") String studentId,
                    @Param("courseId") String courseId);
}
