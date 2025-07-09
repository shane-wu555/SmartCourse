package com.sx.backend.service.impl;

import com.sx.backend.dto.*;
import com.sx.backend.entity.*;
import com.sx.backend.mapper.*;
import com.sx.backend.service.OllamaService;
import com.sx.backend.service.RecommendationService;
import com.sx.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI推荐服务实现
 */
@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private GradeMapper gradeMapper;
    
    @Autowired
    private TaskGradeMapper taskGradeMapper;
    
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private OllamaService ollamaService;
    
    @Override
    public RecommendationResponse generateRecommendation(RecommendationRequest request) {
        log.info("生成推荐，学生ID: {}, 课程ID: {}, 类型: {}", 
                request.getStudentId(), request.getCourseId(), request.getType());
        
        switch (request.getType()) {
            case "knowledge_point":
                return getKnowledgePointRecommendations(request.getStudentId(), 
                        request.getCourseId(), request.getLimit());
            case "resource":
                return getResourceRecommendations(request.getStudentId(), 
                        request.getCourseId(), request.getLimit());
            case "comprehensive":
                return getComprehensiveRecommendations(request.getStudentId(), request.getCourseId());
            default:
                throw new BusinessException(400, "不支持的推荐类型: " + request.getType());
        }
    }
    
    @Override
    public RecommendationResponse getKnowledgePointRecommendations(String studentId, String courseId, int limit) {
        log.info("获取知识点推荐，学生ID: {}, 课程ID: {}, 限制: {}", studentId, courseId, limit);
        
        // 1. 获取学生成绩信息
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) {
            throw new BusinessException(404, "未找到学生成绩信息");
        }
        
        // 2. 获取学生的任务成绩
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(studentId, courseId);
        
        // 3. 获取课程所有知识点
        List<KnowledgePoint> allKnowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        
        // 4. 分析学生的薄弱知识点
        List<KnowledgePointRecommendation> recommendations = analyzeWeakKnowledgePoints(
                studentId, courseId, taskGrades, allKnowledgePoints, limit);
        
        // 5. 生成AI建议
        String aiSuggestion = generateAIKnowledgePointSuggestion(grade, taskGrades, recommendations);
        
        return RecommendationResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .type("knowledge_point")
                .knowledgePointRecommendations(recommendations)
                .overallSuggestion(aiSuggestion)
                .currentGrade(grade.getFinalGrade())
                .classRank(grade.getRankInClass())
                .generatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
    
    @Override
    public RecommendationResponse getResourceRecommendations(String studentId, String courseId, int limit) {
        log.info("获取资源推荐，学生ID: {}, 课程ID: {}, 限制: {}", studentId, courseId, limit);
        
        // 1. 获取学生成绩信息
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) {
            throw new BusinessException(404, "未找到学生成绩信息");
        }
        
        // 2. 获取学生的任务成绩
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(studentId, courseId);
        
        // 3. 获取课程所有资源
        List<Resource> allResources = resourceMapper.getResourcesByCourseId(courseId, null, 0, 1000);
        
        // 4. 分析推荐资源
        List<ResourceRecommendation> recommendations = analyzeRecommendedResources(
                studentId, courseId, taskGrades, allResources, limit);
        
        // 5. 生成AI建议
        String aiSuggestion = generateAIResourceSuggestion(grade, taskGrades, recommendations);
        
        return RecommendationResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .type("resource")
                .resourceRecommendations(recommendations)
                .overallSuggestion(aiSuggestion)
                .currentGrade(grade.getFinalGrade())
                .classRank(grade.getRankInClass())
                .generatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
    
    @Override
    public RecommendationResponse getComprehensiveRecommendations(String studentId, String courseId) {
        log.info("获取综合推荐，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        // 1. 获取知识点推荐
        RecommendationResponse knowledgePointRec = getKnowledgePointRecommendations(studentId, courseId, 3);
        
        // 2. 获取资源推荐
        RecommendationResponse resourceRec = getResourceRecommendations(studentId, courseId, 5);
        
        // 3. 获取学生成绩信息
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(studentId, courseId);
        
        // 4. 生成综合AI建议
        String comprehensiveSuggestion = generateComprehensiveAISuggestion(grade, taskGrades, 
                knowledgePointRec.getKnowledgePointRecommendations(), 
                resourceRec.getResourceRecommendations());
        
        // 5. 计算学习状态和预期提升
        String learningStatus = analyzeLearningStatus(grade, taskGrades);
        Float expectedImprovement = calculateExpectedImprovement(grade, taskGrades);
        String learningPath = generateLearningPath(knowledgePointRec.getKnowledgePointRecommendations());
        
        return RecommendationResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .type("comprehensive")
                .knowledgePointRecommendations(knowledgePointRec.getKnowledgePointRecommendations())
                .resourceRecommendations(resourceRec.getResourceRecommendations())
                .overallSuggestion(comprehensiveSuggestion)
                .currentGrade(grade.getFinalGrade())
                .classRank(grade.getRankInClass())
                .learningStatus(learningStatus)
                .learningPath(learningPath)
                .expectedImprovement(expectedImprovement)
                .generatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
    
    /**
     * 分析学生的薄弱知识点
     */
    private List<KnowledgePointRecommendation> analyzeWeakKnowledgePoints(
            String studentId, String courseId, List<TaskGrade> taskGrades, 
            List<KnowledgePoint> allKnowledgePoints, int limit) {
        
        List<KnowledgePointRecommendation> recommendations = new ArrayList<>();
        
        // 计算每个知识点的掌握程度
        Map<String, Float> knowledgePointMastery = calculateKnowledgePointMastery(taskGrades, courseId);
        
        for (KnowledgePoint kp : allKnowledgePoints) {
            Float masteryLevel = knowledgePointMastery.getOrDefault(kp.getPointId(), 0.0f);
            
            // 判断是否为薄弱知识点（掌握程度低于60%）
            boolean isWeakPoint = masteryLevel < 60.0f;
            
            if (isWeakPoint) {
                // 获取知识点关联的资源数量
                List<Resource> resources = resourceMapper.getResourcesByKnowledgePointId(kp.getPointId());
                
                KnowledgePointRecommendation recommendation = KnowledgePointRecommendation.builder()
                        .pointId(kp.getPointId())
                        .name(kp.getName())
                        .description(kp.getDescription())
                        .difficultyLevel(kp.getDifficultylevel() != null ? kp.getDifficultylevel().name() : "MEDIUM")
                        .reason(generateKnowledgePointReason(masteryLevel, kp.getDifficultylevel()))
                        .priority(calculateKnowledgePointPriority(masteryLevel, kp.getDifficultylevel()))
                        .masteryLevel(masteryLevel)
                        .resourceCount(resources.size())
                        .isWeakPoint(true)
                        .build();
                
                recommendations.add(recommendation);
            }
        }
        
        // 按优先级排序并限制数量
        return recommendations.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 分析推荐资源
     */
    private List<ResourceRecommendation> analyzeRecommendedResources(
            String studentId, String courseId, List<TaskGrade> taskGrades, 
            List<Resource> allResources, int limit) {
        
        List<ResourceRecommendation> recommendations = new ArrayList<>();
        
        // 获取学生的薄弱知识点
        List<KnowledgePoint> allKnowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        Map<String, Float> knowledgePointMastery = calculateKnowledgePointMastery(taskGrades, courseId);
        
        // 找出薄弱知识点
        Set<String> weakKnowledgePoints = new HashSet<>();
        for (KnowledgePoint kp : allKnowledgePoints) {
            Float masteryLevel = knowledgePointMastery.getOrDefault(kp.getPointId(), 0.0f);
            if (masteryLevel < 60.0f) {
                weakKnowledgePoints.add(kp.getPointId());
            }
        }
        
        // 推荐与薄弱知识点相关的资源
        for (Resource resource : allResources) {
            // 简化逻辑：假设每个资源都可能与某个知识点相关
            for (String weakPointId : weakKnowledgePoints) {
                KnowledgePoint relatedKP = knowledgePointMapper.selectKnowledgePointById(weakPointId);
                if (relatedKP != null) {
                    ResourceRecommendation recommendation = ResourceRecommendation.builder()
                            .resourceId(resource.getResourceId())
                            .name(resource.getName())
                            .type(resource.getType().name())
                            .url(resource.getUrl())
                            .description(resource.getDescription())
                            .reason(generateResourceReason(resource.getType(), relatedKP.getName()))
                            .priority(calculateResourcePriority(resource.getType(), resource.getViewCount()))
                            .relatedKnowledgePointId(relatedKP.getPointId())
                            .relatedKnowledgePointName(relatedKP.getName())
                            .size(resource.getSize())
                            .duration(resource.getDuration())
                            .viewCount(resource.getViewCount())
                            .isHighPriority(resource.getViewCount() > 10 || resource.getType() == ResourceType.VIDEO)
                            .build();
                    
                    recommendations.add(recommendation);
                    break; // 每个资源只推荐一次
                }
            }
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        // 按优先级排序并限制数量
        return recommendations.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算知识点掌握程度
     */
    private Map<String, Float> calculateKnowledgePointMastery(List<TaskGrade> taskGrades, String courseId) {
        Map<String, Float> mastery = new HashMap<>();
        
        for (TaskGrade taskGrade : taskGrades) {
            // 获取任务关联的知识点
            List<KnowledgePoint> taskKnowledgePoints = taskMapper.selectPointsByTaskId(taskGrade.getTaskId());
            
            for (KnowledgePoint kp : taskKnowledgePoints) {
                // 简化计算：基于任务成绩计算知识点掌握程度
                Float currentMastery = mastery.getOrDefault(kp.getPointId(), 0.0f);
                Float taskScore = taskGrade.getScore();
                
                // 获取任务最大分数
                Task task = taskMapper.getById(taskGrade.getTaskId());
                if (task != null && task.getMaxScore() != null && task.getMaxScore() > 0) {
                    Float scorePercentage = (taskScore / task.getMaxScore()) * 100;
                    // 取平均值或最大值
                    mastery.put(kp.getPointId(), Math.max(currentMastery, scorePercentage));
                }
            }
        }
        
        return mastery;
    }
    
    /**
     * 生成知识点推荐理由
     */
    private String generateKnowledgePointReason(Float masteryLevel, DifficultyLevel difficulty) {
        if (masteryLevel < 40) {
            return "该知识点掌握程度较低(" + String.format("%.1f", masteryLevel) + "%)，建议重点学习";
        } else if (masteryLevel < 60) {
            return "该知识点掌握程度一般(" + String.format("%.1f", masteryLevel) + "%)，需要进一步巩固";
        } else {
            return "该知识点有提升空间(" + String.format("%.1f", masteryLevel) + "%)，建议深入学习";
        }
    }
    
    /**
     * 计算知识点推荐优先级
     */
    private Integer calculateKnowledgePointPriority(Float masteryLevel, DifficultyLevel difficulty) {
        int priority = 5; // 基础优先级
        
        // 根据掌握程度调整优先级
        if (masteryLevel < 40) {
            priority += 3; // 掌握程度很低，优先级高
        } else if (masteryLevel < 60) {
            priority += 2; // 掌握程度一般，中等优先级
        } else {
            priority += 1; // 掌握程度还可以，低优先级
        }
        
        // 根据难度调整优先级
        if (difficulty == DifficultyLevel.EASY) {
            priority += 2; // 简单的优先学习
        } else if (difficulty == DifficultyLevel.MEDIUM) {
            priority += 1; // 中等难度次之
        }
        // 困难的不额外加分
        
        return Math.min(priority, 10); // 最大优先级为10
    }
    
    /**
     * 生成资源推荐理由
     */
    private String generateResourceReason(ResourceType type, String knowledgePointName) {
        switch (type) {
            case VIDEO:
                return "视频资源有助于直观理解「" + knowledgePointName + "」相关概念";
            case DOCUMENT:
                return "文档资源提供「" + knowledgePointName + "」的详细理论说明";
            case PPT:
                return "PPT资源系统梳理「" + knowledgePointName + "」的知识框架";
            case PDF:
                return "PDF资源深入阐述「" + knowledgePointName + "」的核心内容";
            default:
                return "该资源有助于加深对「" + knowledgePointName + "」的理解";
        }
    }
    
    /**
     * 计算资源推荐优先级
     */
    private Integer calculateResourcePriority(ResourceType type, Integer viewCount) {
        int priority = 5; // 基础优先级
        
        // 根据资源类型调整优先级
        switch (type) {
            case VIDEO:
                priority += 3; // 视频资源优先级高
                break;
            case DOCUMENT:
                priority += 2; // 文档资源次之
                break;
            case PPT:
                priority += 2; // PPT资源次之
                break;
            case PDF:
                priority += 1; // PDF资源一般
                break;
            default:
                priority += 1;
                break;
        }
        
        // 根据观看次数调整优先级
        if (viewCount != null) {
            if (viewCount > 50) {
                priority += 2; // 热门资源
            } else if (viewCount > 20) {
                priority += 1; // 受欢迎资源
            }
        }
        
        return Math.min(priority, 10); // 最大优先级为10
    }
    
    /**
     * 生成AI知识点建议
     */
    private String generateAIKnowledgePointSuggestion(Grade grade, List<TaskGrade> taskGrades, 
            List<KnowledgePointRecommendation> recommendations) {
        try {
            // 构建AI提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为教育专家，请基于以下学生成绩数据，为学生提供知识点学习建议：\\n\\n");
            prompt.append("学生总成绩：").append(grade.getFinalGrade()).append("\\n");
            prompt.append("班级排名：").append(grade.getRankInClass()).append("\\n");
            prompt.append("任务成绩情况：\\n");
            
            for (TaskGrade taskGrade : taskGrades) {
                Task task = taskMapper.getById(taskGrade.getTaskId());
                if (task != null) {
                    prompt.append("- ").append(task.getTitle()).append("：")
                           .append(taskGrade.getScore()).append("/").append(task.getMaxScore()).append("\\n");
                }
            }
            
            prompt.append("\\n推荐重点学习的知识点：\\n");
            for (KnowledgePointRecommendation rec : recommendations) {
                prompt.append("- ").append(rec.getName()).append("（掌握程度：")
                       .append(String.format("%.1f", rec.getMasteryLevel())).append("%）\\n");
            }
            
            prompt.append("\\n请提供简明扼要的学习建议（100字以内）：");
            
            // 调用AI服务
            String aiResponse = ollamaService.generateRecommendationSuggestion(prompt.toString());
            
            // 提取建议内容
            return extractSuggestionFromAIResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("生成AI知识点建议失败", e);
            return generateDefaultKnowledgePointSuggestion(grade, recommendations);
        }
    }
    
    /**
     * 生成AI资源建议
     */
    private String generateAIResourceSuggestion(Grade grade, List<TaskGrade> taskGrades, 
            List<ResourceRecommendation> recommendations) {
        try {
            // 构建AI提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为教育专家，请基于以下学生成绩数据，为学生提供学习资源建议：\\n\\n");
            prompt.append("学生总成绩：").append(grade.getFinalGrade()).append("\\n");
            prompt.append("班级排名：").append(grade.getRankInClass()).append("\\n");
            
            prompt.append("\\n推荐的学习资源：\\n");
            for (ResourceRecommendation rec : recommendations) {
                prompt.append("- ").append(rec.getName()).append("（").append(rec.getType()).append("）")
                       .append(" - ").append(rec.getRelatedKnowledgePointName()).append("\\n");
            }
            
            prompt.append("\\n请提供简明扼要的资源使用建议（100字以内）：");
            
            // 调用AI服务
            String aiResponse = ollamaService.generateRecommendationSuggestion(prompt.toString());
            
            // 提取建议内容
            return extractSuggestionFromAIResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("生成AI资源建议失败", e);
            return generateDefaultResourceSuggestion(grade, recommendations);
        }
    }
    
    /**
     * 生成综合AI建议
     */
    private String generateComprehensiveAISuggestion(Grade grade, List<TaskGrade> taskGrades,
            List<KnowledgePointRecommendation> kpRecommendations, 
            List<ResourceRecommendation> resourceRecommendations) {
        try {
            // 构建AI提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为教育专家，请基于以下学生的全面学习数据，提供综合性学习建议：\\n\\n");
            prompt.append("学生总成绩：").append(grade.getFinalGrade()).append("\\n");
            prompt.append("班级排名：").append(grade.getRankInClass()).append("\\n");
            
            prompt.append("\\n薄弱知识点：\\n");
            for (KnowledgePointRecommendation rec : kpRecommendations) {
                prompt.append("- ").append(rec.getName()).append("（掌握：")
                       .append(String.format("%.1f", rec.getMasteryLevel())).append("%）\\n");
            }
            
            prompt.append("\\n推荐学习资源：\\n");
            for (ResourceRecommendation rec : resourceRecommendations) {
                prompt.append("- ").append(rec.getName()).append("（").append(rec.getType()).append("）\\n");
            }
            
            prompt.append("\\n请提供全面的学习改进建议，包括学习方法、时间安排等（200字以内）：");
            
            // 调用AI服务
            String aiResponse = ollamaService.generateRecommendationSuggestion(prompt.toString());
            
            // 提取建议内容
            return extractSuggestionFromAIResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("生成综合AI建议失败", e);
            return generateDefaultComprehensiveSuggestion(grade, kpRecommendations, resourceRecommendations);
        }
    }
    
    /**
     * 从AI响应中提取建议内容
     */
    private String extractSuggestionFromAIResponse(String aiResponse) {
        // AI服务已经返回了清理后的纯文本内容，直接返回即可
        return aiResponse != null ? aiResponse.trim() : "暂无建议";
    }
    
    /**
     * 分析学习状态
     */
    private String analyzeLearningStatus(Grade grade, List<TaskGrade> taskGrades) {
        if (grade.getFinalGrade() >= 90) {
            return "优秀";
        } else if (grade.getFinalGrade() >= 80) {
            return "良好";
        } else if (grade.getFinalGrade() >= 70) {
            return "中等";
        } else if (grade.getFinalGrade() >= 60) {
            return "及格";
        } else {
            return "需要加强";
        }
    }
    
    /**
     * 计算预期提升空间
     */
    private Float calculateExpectedImprovement(Grade grade, List<TaskGrade> taskGrades) {
        // 基于当前成绩和任务完成情况计算预期提升空间
        Float currentGrade = grade.getFinalGrade();
        if (currentGrade >= 90) {
            return 5.0f; // 优秀学生提升空间有限
        } else if (currentGrade >= 80) {
            return 10.0f; // 良好学生有一定提升空间
        } else if (currentGrade >= 70) {
            return 15.0f; // 中等学生提升空间较大
        } else if (currentGrade >= 60) {
            return 20.0f; // 及格学生提升空间很大
        } else {
            return 25.0f; // 不及格学生提升空间最大
        }
    }
    
    /**
     * 生成学习路径
     */
    private String generateLearningPath(List<KnowledgePointRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return "继续巩固已掌握的知识点";
        }
        
        StringBuilder path = new StringBuilder();
        path.append("建议学习路径：");
        
        // 按优先级排序
        List<KnowledgePointRecommendation> sortedRecs = recommendations.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedRecs.size(); i++) {
            if (i > 0) {
                path.append(" → ");
            }
            path.append(sortedRecs.get(i).getName());
        }
        
        return path.toString();
    }
    
    /**
     * 生成默认知识点建议
     */
    private String generateDefaultKnowledgePointSuggestion(Grade grade, List<KnowledgePointRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return "您的各项知识点掌握情况良好，建议继续保持当前学习状态。";
        }
        
        StringBuilder suggestion = new StringBuilder();
        suggestion.append("根据您的学习情况，建议重点关注以下知识点：");
        
        for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
            KnowledgePointRecommendation rec = recommendations.get(i);
            suggestion.append("「").append(rec.getName()).append("」");
            if (i < Math.min(3, recommendations.size()) - 1) {
                suggestion.append("、");
            }
        }
        
        suggestion.append("。建议通过练习和复习来提高这些知识点的掌握程度。");
        return suggestion.toString();
    }
    
    /**
     * 生成默认资源建议
     */
    private String generateDefaultResourceSuggestion(Grade grade, List<ResourceRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return "当前课程资源丰富，建议根据个人学习需要选择合适的资源进行学习。";
        }
        
        StringBuilder suggestion = new StringBuilder();
        suggestion.append("推荐您优先学习以下资源：");
        
        for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
            ResourceRecommendation rec = recommendations.get(i);
            suggestion.append("「").append(rec.getName()).append("」");
            if (i < Math.min(3, recommendations.size()) - 1) {
                suggestion.append("、");
            }
        }
        
        suggestion.append("。这些资源与您的薄弱知识点密切相关，有助于提高学习效果。");
        return suggestion.toString();
    }
    
    /**
     * 生成默认综合建议
     */
    private String generateDefaultComprehensiveSuggestion(Grade grade, 
            List<KnowledgePointRecommendation> kpRecommendations, 
            List<ResourceRecommendation> resourceRecommendations) {
        
        StringBuilder suggestion = new StringBuilder();
        
        // 基于成绩给出总体评价
        if (grade.getFinalGrade() >= 80) {
            suggestion.append("您的学习表现良好，");
        } else if (grade.getFinalGrade() >= 60) {
            suggestion.append("您的学习表现中等，");
        } else {
            suggestion.append("您需要加强学习努力，");
        }
        
        // 知识点建议
        if (!kpRecommendations.isEmpty()) {
            suggestion.append("建议重点关注").append(kpRecommendations.size()).append("个薄弱知识点；");
        }
        
        // 资源建议
        if (!resourceRecommendations.isEmpty()) {
            suggestion.append("推荐您学习").append(resourceRecommendations.size()).append("个相关资源；");
        }
        
        suggestion.append("制定合理的学习计划，循序渐进地提高学习效果。");
        
        return suggestion.toString();
    }
}
