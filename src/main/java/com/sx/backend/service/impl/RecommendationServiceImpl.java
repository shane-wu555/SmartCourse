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
        
        // 5. 计算任务总分
        Float maxScore = calculateTotalMaxScore(taskGrades, courseId);
        
        // 6. 生成AI建议
        String aiSuggestion = generateAIKnowledgePointSuggestion(grade, taskGrades, recommendations);
        
        return RecommendationResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .type("knowledge_point")
                .knowledgePointRecommendations(recommendations)
                .overallSuggestion(aiSuggestion)
                .currentGrade(grade.getFinalGrade())
                .maxScore(maxScore)
                .classRank(grade.getRankInClass())
                .generatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
    
    @Override
    public RecommendationResponse getResourceRecommendations(String studentId, String courseId, int limit) {
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
        
        // 5. 计算任务总分
        Float maxScore = calculateTotalMaxScore(taskGrades, courseId);
        
        // 6. 生成AI建议
        String aiSuggestion = generateAIResourceSuggestion(grade, taskGrades, recommendations);
        
        return RecommendationResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .type("resource")
                .resourceRecommendations(recommendations)
                .overallSuggestion(aiSuggestion)
                .currentGrade(grade.getFinalGrade())
                .maxScore(maxScore)
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
        
        // 5. 计算任务总分
        Float maxScore = calculateTotalMaxScore(taskGrades, courseId);
        
        // 6. 计算学习状态和预期提升
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
                .maxScore(maxScore)
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
        
        // 检查任务是否绑定了知识点
        boolean hasKnowledgePointBinding = checkHasTaskKnowledgePointBinding(taskGrades, courseId);
        
        if (hasKnowledgePointBinding) {
            // 传统方式：基于知识点掌握程度分析
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
        } else {
            // 新方式：基于成绩和排名推荐知识点
            Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
            recommendations = generateKnowledgePointRecommendationsByGrade(grade, allKnowledgePoints, limit);
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
        
        // 检查任务是否绑定了知识点
        boolean hasKnowledgePointBinding = checkHasTaskKnowledgePointBinding(taskGrades, courseId);
        
        if (hasKnowledgePointBinding) {
            // 传统方式：基于知识点分析推荐资源
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
        } else {
            // 新方式：基于成绩和排名推荐资源
            Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
            recommendations = generateResourceRecommendationsByGrade(grade, allResources, courseId, limit);
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
            // 检查是否基于知识点绑定生成的推荐
            boolean hasKnowledgePointBinding = checkHasTaskKnowledgePointBinding(taskGrades, grade.getCourseId());
            
            // 计算实际满分和得分率
            Float maxScore = calculateTotalMaxScore(taskGrades, grade.getCourseId());
            float gradePercentage = calculateActualGradePercentage(grade.getFinalGrade(), maxScore);
            
            // 构建AI提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为教育专家，请基于以下学生成绩数据，为学生提供学习建议：\\n\\n");
            prompt.append("学生总成绩：").append(grade.getFinalGrade()).append("分（满分").append(maxScore).append("分）\\n");
            prompt.append("得分率：").append(String.format("%.1f", gradePercentage)).append("%\\n");
            prompt.append("班级排名：").append(grade.getRankInClass()).append("\\n");
            
            if (hasKnowledgePointBinding) {
                // 传统基于知识点掌握程度的建议
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
            } else {
                // 基于成绩和排名的建议
                prompt.append("\\n注意：当前课程任务未绑定具体知识点，推荐内容基于学生成绩和排名分析。\\n");
                prompt.append("\\n基于成绩推荐的学习知识点：\\n");
                for (KnowledgePointRecommendation rec : recommendations) {
                    prompt.append("- ").append(rec.getName()).append("（").append(rec.getDifficultyLevel()).append("难度）");
                    if (rec.getReason() != null) {
                        prompt.append(" - ").append(rec.getReason());
                    }
                    prompt.append("\\n");
                }
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
            // 检查是否基于知识点绑定生成的推荐
            boolean hasKnowledgePointBinding = checkHasTaskKnowledgePointBinding(taskGrades, grade.getCourseId());
            
            // 计算实际满分和得分率
            Float maxScore = calculateTotalMaxScore(taskGrades, grade.getCourseId());
            float gradePercentage = calculateActualGradePercentage(grade.getFinalGrade(), maxScore);
            
            // 构建AI提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为教育专家，请基于以下学生成绩数据，为学生提供学习资源建议：\\n\\n");
            prompt.append("学生总成绩：").append(grade.getFinalGrade()).append("分（满分").append(maxScore).append("分）\\n");
            prompt.append("得分率：").append(String.format("%.1f", gradePercentage)).append("%\\n");
            prompt.append("班级排名：").append(grade.getRankInClass()).append("\\n");
            
            if (!hasKnowledgePointBinding) {
                prompt.append("\\n注意：推荐资源基于学生成绩和学习水平分析，非特定知识点关联。\\n");
            }
            
            prompt.append("\\n推荐的学习资源：\\n");
            for (ResourceRecommendation rec : recommendations) {
                prompt.append("- ").append(rec.getName()).append("（").append(rec.getType()).append("）");
                if (rec.getRelatedKnowledgePointName() != null) {
                    prompt.append(" - ").append(rec.getRelatedKnowledgePointName());
                }
                prompt.append("\\n");
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
            Float finalGrade = grade.getFinalGrade();
            Integer classRank = grade.getRankInClass();
            
            // 计算实际满分和得分率
            Float maxScore = calculateTotalMaxScore(taskGrades, grade.getCourseId());
            float gradePercentage = calculateActualGradePercentage(finalGrade, maxScore);
            
            // 构建AI提示词
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为一名智能学习助手，请为学生提供个性化的学习建议。\\n\\n");
            
            // 学生基本信息
            prompt.append("学生成绩情况：\\n");
            prompt.append("- 总成绩：").append(finalGrade).append("分（满分").append(maxScore).append("分）\\n");
            prompt.append("- 得分率：").append(String.format("%.1f", gradePercentage)).append("%\\n");
            if (classRank != null) {
                prompt.append("- 班级排名：第").append(classRank).append("名\\n");
            }
            
            // 任务成绩详情
            if (!taskGrades.isEmpty()) {
                prompt.append("\\n具体任务成绩：\\n");
                for (TaskGrade taskGrade : taskGrades) {
                    prompt.append("- 任务ID ").append(taskGrade.getTaskId()).append("：")
                          .append(taskGrade.getScore()).append("分\\n");
                }
            }
            
            // 薄弱知识点
            if (!kpRecommendations.isEmpty()) {
                prompt.append("\\n需要重点关注的知识点：\\n");
                for (KnowledgePointRecommendation rec : kpRecommendations) {
                    prompt.append("- ").append(rec.getName()).append("（掌握程度：")
                          .append(String.format("%.1f", rec.getMasteryLevel())).append("%）\\n");
                }
            }
            
            // 推荐资源
            if (!resourceRecommendations.isEmpty()) {
                prompt.append("\\n推荐学习资源：\\n");
                for (ResourceRecommendation rec : resourceRecommendations) {
                    prompt.append("- ").append(rec.getName()).append("（").append(rec.getType()).append("）\\n");
                }
            }
            
            // 根据学生水平调整提示词
            if (gradePercentage >= 95 && (classRank == null || classRank <= 3)) {
                prompt.append("\\n该学生成绩优异，请提供拓展性和挑战性的学习建议。");
            } else if (gradePercentage >= 85 && (classRank == null || classRank <= 10)) {
                prompt.append("\\n该学生成绩良好，请提供进阶学习建议。");
            } else if (gradePercentage >= 70) {
                prompt.append("\\n该学生成绩中等，请提供基础巩固建议。");
            } else {
                prompt.append("\\n该学生成绩需要提升，请提供基础加强建议。");
            }
            
            prompt.append("\\n\\n请提供简明扼要的综合学习建议（150字以内，要求：温馨友好，具体可行，符合学生实际水平）：");
            
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
        Float finalGrade = grade.getFinalGrade();
        Integer classRank = grade.getRankInClass();
        
        // 计算实际满分和得分率
        Float maxScore = calculateTotalMaxScore(taskGrades, grade.getCourseId());
        float gradePercentage = calculateActualGradePercentage(finalGrade, maxScore);
        
        if (gradePercentage >= 95 && (classRank == null || classRank <= 3)) {
            return "优秀";
        } else if (gradePercentage >= 85 && (classRank == null || classRank <= 10)) {
            return "良好";  
        } else if (gradePercentage >= 70) {
            return "中等";
        } else if (gradePercentage >= 60) {
            return "及格";
        } else {
            return "需要加强";
        }
    }
    
    /**
     * 计算预期提升空间
     */
    private Float calculateExpectedImprovement(Grade grade, List<TaskGrade> taskGrades) {
        Float finalGrade = grade.getFinalGrade();
        Integer classRank = grade.getRankInClass();
        
        // 计算实际满分和得分率
        Float maxScore = calculateTotalMaxScore(taskGrades, grade.getCourseId());
        float gradePercentage = calculateActualGradePercentage(finalGrade, maxScore);
        
        // 如果已经是第一名且得分率很高，提升空间很小
        if (classRank != null && classRank == 1 && gradePercentage >= 95) {
            return 0.0f; // 已经非常优秀，无需提升
        } else if (gradePercentage >= 90 && (classRank == null || classRank <= 5)) {
            return Math.max(0.0f, (100 - gradePercentage) * 0.3f); // 很小的提升空间
        } else if (gradePercentage >= 80) {
            return (95 - gradePercentage) * 0.6f; // 适度提升空间
        } else if (gradePercentage >= 70) {
            return (85 - gradePercentage) * 0.8f; // 较大提升空间
        } else if (gradePercentage >= 60) {
            return (80 - gradePercentage) * 0.9f; // 很大提升空间
        } else {
            return (75 - gradePercentage) * 1.0f; // 最大提升空间
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
        
        // 检查是否为基于成绩的推荐
        boolean isGradeBased = recommendations.stream()
                .anyMatch(rec -> rec.getReason() != null && (rec.getReason().contains("建议学习") || rec.getReason().contains("您的成绩优秀")));
        
        if (isGradeBased) {
            // 基于成绩的推荐建议
            if (grade.getFinalGrade() >= 85) {
                suggestion.append("基于您的优秀成绩，建议学习以下进阶知识点：");
            } else if (grade.getFinalGrade() >= 70) {
                suggestion.append("根据您的学习水平，建议重点掌握以下知识点：");
            } else {
                suggestion.append("建议从以下基础知识点开始学习：");
            }
        } else {
            // 传统基于掌握程度的推荐建议
            suggestion.append("根据您的学习情况，建议重点关注以下知识点：");
        }
        
        for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
            KnowledgePointRecommendation rec = recommendations.get(i);
            suggestion.append("「").append(rec.getName()).append("」");
            if (i < Math.min(3, recommendations.size()) - 1) {
                suggestion.append("、");
            }
        }
        
        if (isGradeBased) {
            suggestion.append("。这些知识点符合您当前的学习水平，有助于稳步提升学习效果。");
        } else {
            suggestion.append("。建议通过练习和复习来提高这些知识点的掌握程度。");
        }
        
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
        
        // 检查是否为基于成绩的推荐
        boolean isGradeBased = recommendations.stream()
                .anyMatch(rec -> rec.getReason() != null && rec.getReason().contains("基于您的"));
        
        if (isGradeBased) {
            // 基于成绩的资源推荐建议
            if (grade.getFinalGrade() >= 85) {
                suggestion.append("基于您的优秀成绩，推荐以下深度学习资源：");
            } else if (grade.getFinalGrade() >= 70) {
                suggestion.append("根据您的学习水平，推荐以下巩固学习资源：");
            } else {
                suggestion.append("建议您优先学习以下基础资源：");
            }
        } else {
            // 传统推荐建议
            suggestion.append("推荐您优先学习以下资源：");
        }
        
        for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
            ResourceRecommendation rec = recommendations.get(i);
            suggestion.append("「").append(rec.getName()).append("」");
            if (i < Math.min(3, recommendations.size()) - 1) {
                suggestion.append("、");
            }
        }
        
        if (isGradeBased) {
            suggestion.append("。这些资源类型适合您当前的学习水平，有助于进一步提升学习效果。");
        } else {
            suggestion.append("。这些资源与您的薄弱知识点密切相关，有助于提高学习效果。");
        }
        
        return suggestion.toString();
    }
    
    /**
     * 生成默认综合建议
     */
    private String generateDefaultComprehensiveSuggestion(Grade grade, 
            List<KnowledgePointRecommendation> kpRecommendations, 
            List<ResourceRecommendation> resourceRecommendations) {
        
        StringBuilder suggestion = new StringBuilder();
        
        // 检查是否为基于成绩的推荐
        boolean isGradeBased = kpRecommendations.stream()
                .anyMatch(rec -> rec.getReason() != null && (rec.getReason().contains("建议学习") || rec.getReason().contains("您的成绩优秀")));
        
        // 基于成绩给出总体评价
        if (grade.getFinalGrade() >= 85) {
            suggestion.append("您的学习表现优秀，");
        } else if (grade.getFinalGrade() >= 70) {
            suggestion.append("您的学习表现良好，");
        } else {
            suggestion.append("建议您加强学习努力，");
        }
        
        // 根据推荐方式给出不同建议
        if (isGradeBased) {
            suggestion.append("基于成绩分析为您推荐了");
            if (!kpRecommendations.isEmpty()) {
                suggestion.append(kpRecommendations.size()).append("个适合的知识点");
            }
            if (!resourceRecommendations.isEmpty()) {
                if (!kpRecommendations.isEmpty()) {
                    suggestion.append("和");
                }
                suggestion.append(resourceRecommendations.size()).append("个学习资源");
            }
            suggestion.append("；推荐内容基于您的成绩水平和班级排名，建议按照推荐的难度层次循序渐进地学习。");
        } else {
            // 传统知识点分析建议
            if (!kpRecommendations.isEmpty()) {
                suggestion.append("建议重点关注").append(kpRecommendations.size()).append("个薄弱知识点；");
            }
            
            if (!resourceRecommendations.isEmpty()) {
                suggestion.append("推荐您学习").append(resourceRecommendations.size()).append("个相关资源；");
            }
            
            suggestion.append("制定合理的学习计划，循序渐进地提高学习效果。");
        }
        
        return suggestion.toString();
    }
    
    /**
     * 检查当前课程任务是否有知识点绑定
     */
    private boolean checkHasTaskKnowledgePointBinding(List<TaskGrade> taskGrades, String courseId) {
        for (TaskGrade taskGrade : taskGrades) {
            List<KnowledgePoint> taskKnowledgePoints = taskMapper.selectPointsByTaskId(taskGrade.getTaskId());
            if (taskKnowledgePoints != null && !taskKnowledgePoints.isEmpty()) {
                return true; // 至少有一个任务绑定了知识点
            }
        }
        return false; // 没有任务绑定知识点
    }
    
    /**
     * 基于成绩和排名生成知识点推荐
     */
    private List<KnowledgePointRecommendation> generateKnowledgePointRecommendationsByGrade(
            Grade grade, List<KnowledgePoint> allKnowledgePoints, int limit) {
        
        List<KnowledgePointRecommendation> recommendations = new ArrayList<>();
        
        if (grade == null || allKnowledgePoints.isEmpty()) {
            return recommendations;
        }
        
        // 根据成绩和排名确定推荐策略
        Float finalGrade = grade.getFinalGrade();
        Integer classRank = grade.getRankInClass();
        
        // 根据成绩水平分类推荐知识点
        List<KnowledgePoint> targetKnowledgePoints;
        String recommendationType;
        
        // 对于没有任务分数的情况，使用原有的逻辑作为降级方案
        float gradePercentage = calculateGradePercentage(finalGrade);
        
        if (gradePercentage >= 85 && (classRank == null || classRank <= 10)) {
            // 高分学生（得分率85%以上）：推荐难度较高的知识点和进阶内容
            targetKnowledgePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.HARD || 
                                 kp.getDifficultylevel() == DifficultyLevel.MEDIUM)
                    .collect(Collectors.toList());
            recommendationType = "进阶提升";
        } else if (gradePercentage >= 70) {
            // 中等成绩学生（得分率70-85%）：推荐中等难度知识点，巩固基础
            targetKnowledgePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.MEDIUM || 
                                 kp.getDifficultylevel() == DifficultyLevel.EASY)
                    .collect(Collectors.toList());
            recommendationType = "稳步提升";
        } else {
            // 低分学生（得分率70%以下）：优先推荐基础知识点
            targetKnowledgePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.EASY)
                    .collect(Collectors.toList());
            if (targetKnowledgePoints.isEmpty()) {
                // 如果没有简单难度的知识点，则选择中等难度
                targetKnowledgePoints = allKnowledgePoints.stream()
                        .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.MEDIUM)
                        .collect(Collectors.toList());
            }
            recommendationType = "夯实基础";
        }
        
        // 如果筛选后没有知识点，使用全部知识点
        if (targetKnowledgePoints.isEmpty()) {
            targetKnowledgePoints = allKnowledgePoints;
            recommendationType = "全面学习";
        }
        
        // 为目标知识点生成推荐
        for (KnowledgePoint kp : targetKnowledgePoints) {
            List<Resource> resources = resourceMapper.getResourcesByKnowledgePointId(kp.getPointId());
            
            // 计算基于成绩的模拟掌握率（用于展示，不是真实掌握率）
            Float simulatedMasteryLevel = calculateSimulatedMasteryLevel(gradePercentage, kp.getDifficultylevel());
            
            KnowledgePointRecommendation recommendation = KnowledgePointRecommendation.builder()
                    .pointId(kp.getPointId())
                    .name(kp.getName())
                    .description(kp.getDescription())
                    .difficultyLevel(kp.getDifficultylevel() != null ? kp.getDifficultylevel().name() : "MEDIUM")
                    .reason(generateReasonByGrade(gradePercentage, classRank, kp.getDifficultylevel(), recommendationType))
                    .priority(calculatePriorityByGrade(gradePercentage, kp.getDifficultylevel()))
                    .masteryLevel(simulatedMasteryLevel)
                    .resourceCount(resources.size())
                    .isWeakPoint(false) // 基于成绩推荐的知识点不是"薄弱点"，而是"学习目标"
                    .build();
            
            recommendations.add(recommendation);
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        return recommendations;
    }
    
    /**
     * 基于成绩和排名生成资源推荐
     */
    private List<ResourceRecommendation> generateResourceRecommendationsByGrade(
            Grade grade, List<Resource> allResources, String courseId, int limit) {
        
        List<ResourceRecommendation> recommendations = new ArrayList<>();
        
        if (grade == null || allResources.isEmpty()) {
            return recommendations;
        }
        
        Float finalGrade = grade.getFinalGrade();
        // 对于没有任务分数的情况，使用原有的逻辑作为降级方案
        float gradePercentage = calculateGradePercentage(finalGrade);
        Integer classRank = grade.getRankInClass();
        
        // 获取课程所有知识点用于关联
        List<KnowledgePoint> allKnowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        
        // 根据成绩确定资源推荐策略
        List<ResourceType> preferredTypes;
        String recommendationType;
        
        if (gradePercentage >= 85 && (classRank == null || classRank <= 10)) {
            // 高分学生：优先推荐文档、PDF等深度学习资源
            preferredTypes = Arrays.asList(ResourceType.PDF, ResourceType.DOCUMENT, ResourceType.VIDEO, ResourceType.PPT);
            recommendationType = "深度学习";
        } else if (gradePercentage >= 70) {
            // 中等成绩学生：推荐视频、PPT等易理解的资源
            preferredTypes = Arrays.asList(ResourceType.VIDEO, ResourceType.PPT, ResourceType.DOCUMENT, ResourceType.PDF);
            recommendationType = "巩固学习";
        } else {
            // 低分学生：优先推荐视频等直观易懂的资源
            preferredTypes = Arrays.asList(ResourceType.VIDEO, ResourceType.PPT, ResourceType.DOCUMENT, ResourceType.PDF);
            recommendationType = "基础学习";
        }
        
        // 按优先类型排序资源
        List<Resource> sortedResources = allResources.stream()
                .sorted((r1, r2) -> {
                    int priority1 = preferredTypes.indexOf(r1.getType());
                    int priority2 = preferredTypes.indexOf(r2.getType());
                    if (priority1 == -1) priority1 = preferredTypes.size();
                    if (priority2 == -1) priority2 = preferredTypes.size();
                    return Integer.compare(priority1, priority2);
                })
                .collect(Collectors.toList());
        
        // 生成资源推荐
        for (Resource resource : sortedResources) {
            // 为资源关联一个合适的知识点（简化逻辑）
            KnowledgePoint relatedKP = findSuitableKnowledgePointForResource(allKnowledgePoints, gradePercentage);
            
            ResourceRecommendation recommendation = ResourceRecommendation.builder()
                    .resourceId(resource.getResourceId())
                    .name(resource.getName())
                    .type(resource.getType().name())
                    .url(resource.getUrl())
                    .description(resource.getDescription())
                    .reason(generateResourceReasonByGrade(resource.getType(), gradePercentage, recommendationType))
                    .priority(calculateResourcePriorityByGrade(resource.getType(), resource.getViewCount(), gradePercentage))
                    .relatedKnowledgePointId(relatedKP != null ? relatedKP.getPointId() : null)
                    .relatedKnowledgePointName(relatedKP != null ? relatedKP.getName() : "通用学习")
                    .size(resource.getSize())
                    .duration(resource.getDuration())
                    .viewCount(resource.getViewCount())
                    .isHighPriority(preferredTypes.indexOf(resource.getType()) < 2)
                    .build();
            
            recommendations.add(recommendation);
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        return recommendations;
    }
    
    /**
     * 计算基于成绩的模拟掌握率
     */
    private Float calculateSimulatedMasteryLevel(Float gradePercentage, DifficultyLevel difficulty) {
        // 对于基于成绩的推荐，计算适合学习的知识点的"学习适配度"
        // 这不是真实的掌握率，而是基于成绩推算的学习准备程度
        
        if (gradePercentage >= 85) {
            // 高分学生：表示对推荐知识点的学习准备程度
            switch (difficulty) {
                case HARD:
                    return 85.0f; // 已具备挑战高难度的能力
                case MEDIUM:
                    return 92.0f; // 完全准备好学习中等难度
                case EASY:
                    return 98.0f; // 基础扎实，可快速掌握
                default:
                    return 90.0f;
            }
        } else if (gradePercentage >= 70) {
            // 中等成绩学生：表示学习准备程度
            switch (difficulty) {
                case HARD:
                    return 65.0f; // 需要努力可以掌握
                case MEDIUM:
                    return 80.0f; // 适合重点学习
                case EASY:
                    return 90.0f; // 基础较好
                default:
                    return 75.0f;
            }
        } else {
            // 低分学生：表示当前基础水平
            switch (difficulty) {
                case HARD:
                    return 40.0f; // 暂时不太适合
                case MEDIUM:
                    return 55.0f; // 需要逐步提升
                case EASY:
                    return 70.0f; // 重点巩固基础
                default:
                    return 50.0f;
            }
        }
    }
    
    /**
     * 根据成绩生成知识点推荐理由
     */
    private String generateReasonByGrade(Float gradePercentage, Integer classRank, DifficultyLevel difficulty, String type) {
        StringBuilder reason = new StringBuilder();
        
        // 基于成绩的描述
        if (gradePercentage >= 85) {
            reason.append("您的成绩优秀，建议");
        } else if (gradePercentage >= 70) {
            reason.append("您的成绩良好，建议");
        } else {
            reason.append("建议重点");
        }
        
        // 基于推荐类型的建议
        switch (type) {
            case "进阶提升":
                reason.append("学习该").append(difficulty.name().toLowerCase()).append("难度知识点，进一步提升学习深度");
                break;
            case "稳步提升":
                reason.append("掌握该知识点，稳步提升学习水平");
                break;
            case "夯实基础":
                reason.append("从该基础知识点开始，夯实学习基础");
                break;
            default:
                reason.append("学习该知识点，提升整体学习效果");
                break;
        }
        
        return reason.toString();
    }
    
    /**
     * 根据成绩计算知识点推荐优先级
     */
    private Integer calculatePriorityByGrade(Float gradePercentage, DifficultyLevel difficulty) {
        int priority = 5; // 基础优先级
        
        if (gradePercentage >= 85) {
            // 高分学生：困难知识点优先级高
            if (difficulty == DifficultyLevel.HARD) {
                priority += 3;
            } else if (difficulty == DifficultyLevel.MEDIUM) {
                priority += 2;
            } else {
                priority += 1;
            }
        } else if (gradePercentage >= 70) {
            // 中等成绩：中等难度优先级高
            if (difficulty == DifficultyLevel.MEDIUM) {
                priority += 3;
            } else if (difficulty == DifficultyLevel.EASY) {
                priority += 2;
            } else {
                priority += 1;
            }
        } else {
            // 低分学生：简单知识点优先级高
            if (difficulty == DifficultyLevel.EASY) {
                priority += 3;
            } else if (difficulty == DifficultyLevel.MEDIUM) {
                priority += 2;
            } else {
                priority += 1;
            }
        }
        
        return Math.min(priority, 10);
    }
    
    /**
     * 为资源找到合适的知识点关联
     */
    private KnowledgePoint findSuitableKnowledgePointForResource(List<KnowledgePoint> allKnowledgePoints, Float gradePercentage) {
        if (allKnowledgePoints.isEmpty()) {
            return null;
        }
        
        // 根据成绩选择合适难度的知识点
        List<KnowledgePoint> suitablePoints;
        if (gradePercentage >= 85) {
            suitablePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.HARD || 
                                 kp.getDifficultylevel() == DifficultyLevel.MEDIUM)
                    .collect(Collectors.toList());
        } else if (gradePercentage >= 70) {
            suitablePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.MEDIUM)
                    .collect(Collectors.toList());
        } else {
            suitablePoints = allKnowledgePoints.stream()
                    .filter(kp -> kp.getDifficultylevel() == DifficultyLevel.EASY)
                    .collect(Collectors.toList());
        }
        
        if (suitablePoints.isEmpty()) {
            suitablePoints = allKnowledgePoints;
        }
        
        // 返回第一个合适的知识点
        return suitablePoints.get(0);
    }
    
    /**
     * 根据成绩生成资源推荐理由
     */
    private String generateResourceReasonByGrade(ResourceType type, Float gradePercentage, String recommendationType) {
        StringBuilder reason = new StringBuilder();
        
        if (gradePercentage >= 85) {
            reason.append("基于您的优秀成绩，");
        } else if (gradePercentage >= 70) {
            reason.append("根据您的学习水平，");
        } else {
            reason.append("建议您");
        }
        
        switch (type) {
            case VIDEO:
                reason.append("该视频资源有助于").append(recommendationType);
                break;
            case DOCUMENT:
                reason.append("该文档资源适合").append(recommendationType);
                break;
            case PPT:
                reason.append("该PPT资源便于").append(recommendationType);
                break;
            case PDF:
                reason.append("该PDF资源支持").append(recommendationType);
                break;
            default:
                reason.append("该资源有助于").append(recommendationType);
                break;
        }
        
        return reason.toString();
    }
    
    /**
     * 根据成绩计算资源推荐优先级
     */
    private Integer calculateResourcePriorityByGrade(ResourceType type, Integer viewCount, Float gradePercentage) {
        int priority = 5; // 基础优先级
        
        // 根据成绩调整不同资源类型的优先级
        if (gradePercentage >= 85) {
            // 高分学生：偏好文档和PDF
            switch (type) {
                case PDF:
                    priority += 3;
                    break;
                case DOCUMENT:
                    priority += 2;
                    break;
                case VIDEO:
                    priority += 1;
                    break;
                case PPT:
                    priority += 1;
                    break;
                default:
                    priority += 1;
                    break;
            }
        } else {
            // 中低分学生：偏好视频和PPT
            switch (type) {
                case VIDEO:
                    priority += 3;
                    break;
                case PPT:
                    priority += 2;
                    break;
                case DOCUMENT:
                    priority += 1;
                    break;
                case PDF:
                    priority += 1;
                    break;
                default:
                    priority += 1;
                    break;
            }
        }
        
        // 根据观看次数调整优先级
        if (viewCount != null) {
            if (viewCount > 50) {
                priority += 2;
            } else if (viewCount > 20) {
                priority += 1;
            }
        }
        
        return Math.min(priority, 10);
    }
    
    /**
     * 计算任务总分
     */
    private Float calculateTotalMaxScore(List<TaskGrade> taskGrades, String courseId) {
        if (taskGrades == null || taskGrades.isEmpty()) {
            return 0.0f;
        }
        
        Float totalMaxScore = 0.0f;
        for (TaskGrade taskGrade : taskGrades) {
            // 获取任务信息以获取最大分数
            Task task = taskMapper.getById(taskGrade.getTaskId());
            if (task != null && task.getMaxScore() != null) {
                totalMaxScore += task.getMaxScore();
            }
        }
        
        return totalMaxScore;
    }
    
    /**
     * 计算实际成绩百分比，基于真实的满分
     */
    private float calculateActualGradePercentage(Float finalGrade, Float maxScore) {
        if (finalGrade == null || maxScore == null || maxScore == 0) {
            return 0.0f;
        }
        return (finalGrade / maxScore) * 100;
    }
    
    /**
     * 计算成绩百分比，适应不同评分制度
     * @deprecated 使用 calculateActualGradePercentage 替代
     */
    private float calculateGradePercentage(Float finalGrade) {
        if (finalGrade == null) {
            return 0.0f;
        }
        
        // 根据成绩数值判断可能的评分制度
        if (finalGrade <= 1.0f) {
            // 可能是1分制，转换为百分比
            return finalGrade * 100;
        } else if (finalGrade <= 10.0f) {
            // 可能是10分制，转换为百分比
            return (finalGrade / 10.0f) * 100;
        } else if (finalGrade <= 20.0f) {
            // 可能是20分制，转换为百分比
            return (finalGrade / 20.0f) * 100;
        } else if (finalGrade <= 100.0f) {
            // 可能是百分制，直接返回
            return finalGrade;
        } else {
            // 未知制度，假设是百分制
            return Math.min(finalGrade, 100.0f);
        }
    }
}
