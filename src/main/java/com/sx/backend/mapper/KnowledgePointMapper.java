package com.sx.backend.mapper;

import com.sx.backend.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgePointMapper {
    int insertKnowledgePoint(KnowledgePoint knowledgePoint);
    int updateKnowledgePoint(KnowledgePoint knowledgePoint);
    int deleteKnowledgePoint(String pointId);
    KnowledgePoint selectKnowledgePointById(String pointId);
    List<KnowledgePoint> selectKnowledgePointsByCourseId(String courseId);
    int checkNameExists(@Param("courseId") String courseId,
                        @Param("name") String name,
                        @Param("pointId") String pointId);
    int checkHasResources(String pointId);
    int checkHasTasks(String pointId);
}