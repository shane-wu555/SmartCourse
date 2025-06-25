package com.sx.backend.mapper;

import com.sx.backend.entity.KnowledgeRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeRelationMapper {
    int insertKnowledgeRelation(KnowledgeRelation relation);
    int deleteKnowledgeRelationById(String relationId);
    int deleteRelationsByPointId(String pointId);
    int deleteRelationByPoints(@Param("sourceId") String sourceId,
                               @Param("targetId") String targetId);
    List<KnowledgeRelation> selectRelationsByPointId(String pointId);
    int checkRelationExists(@Param("sourceId") String sourceId,
                            @Param("targetId") String targetId);
    int checkCircularDependency(@Param("sourceId") String sourceId,
                                @Param("targetId") String targetId);
     List<KnowledgeRelation> selectRelationsByCourseId(@Param("courseId") String courseId);
}