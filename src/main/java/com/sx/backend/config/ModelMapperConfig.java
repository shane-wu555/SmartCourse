package com.sx.backend.config;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.Course;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 1. 简化全局配置
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);

        // 2. 只添加必要的类型映射
        //addCourseMappings(modelMapper);

        return modelMapper;
    }

    private void addCourseMappings(ModelMapper modelMapper) {
        // Course → CourseDTO 映射
        modelMapper.createTypeMap(Course.class, CourseDTO.class)
                .addMappings(mapper -> {
                    // 直接映射基础字段
                    mapper.map(Course::getCourseId, CourseDTO::setCourseId);
                    mapper.map(Course::getCourseCode, CourseDTO::setCourseCode);
                    mapper.map(Course::getName, CourseDTO::setName);
                    mapper.map(Course::getDescription, CourseDTO::setDescription);
                    mapper.map(Course::getCredit, CourseDTO::setCredit);
                    mapper.map(Course::getHours, CourseDTO::setHours);
                    mapper.map(Course::getSemester, CourseDTO::setSemester);
                    mapper.map(Course::getCreateTime, CourseDTO::setCreateTime);
                    mapper.map(Course::getUpdateTime, CourseDTO::setUpdateTime);

                    // 使用简单表达式映射教师信息
                    mapper.<String>map(
                            src -> src.getTeacherId() != null ? src.getTeacherId() : null,
                            CourseDTO::setTeacherId
                    );

                    mapper.<String>map(
                            src -> src.getTeacherId() != null ,
                            CourseDTO::setTeacherName
                    );

                    // 使用简单表达式映射统计字段
                    mapper.<Integer>map(
                            src -> src.getStudents() != null ? src.getStudents().size() : 0,
                            CourseDTO::setStudentCount
                    );

                    mapper.<Integer>map(
                            src -> src.getTasks() != null ? src.getTasks().size() : 0,
                            CourseDTO::setTaskCount
                    );
                });

        // 其他映射保持类似方式简化
    }
}