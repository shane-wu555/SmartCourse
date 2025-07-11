package com.sx.backend.service.impl;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.entity.PaperGenerationMethod;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.mapper.TestPaperMapper;
import com.sx.backend.service.QuestionService;
import com.sx.backend.service.TestPaperService;
import com.sx.backend.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestPaperServiceImpl implements TestPaperService {
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private TestPaperMapper testPaperMapper;
    
    @Autowired
    private KnowledgePointService knowledgePointService;

    @Override
    public TestPaper generatePaper(GeneratePaperRequestDTO requestDTO) {
        // 获取题库中的所有题目
        List<Question> allQuestions;
        if (requestDTO.getBankId() != null && !requestDTO.getBankId().isEmpty()) {
            // 如果指定了题库ID，直接从题库获取题目
            allQuestions = questionService.getQuestionsByBankId(requestDTO.getBankId());
        } else {
            // 如果没有指定题库ID，抛出异常
            throw new IllegalArgumentException("题库ID不能为空");
        }
        
        List<Question> selected = new ArrayList<>();
        String mode = requestDTO.getMode();
        
        if ("random".equalsIgnoreCase(mode)) {
            // 校验题目数量
            if (allQuestions.size() < requestDTO.getTotalCount()) {
                throw new IllegalArgumentException("题库中可用题目不足，最多可选 " + allQuestions.size() + " 道题");
            }
            Collections.shuffle(allQuestions);
            selected = allQuestions.stream().limit(requestDTO.getTotalCount()).collect(Collectors.toList());
            
        } else if ("knowledge".equalsIgnoreCase(mode)) {
            List<String> kpIds = requestDTO.getKnowledgePointIds();
            if (kpIds == null || kpIds.isEmpty()) {
                throw new IllegalArgumentException("按知识点组卷时，知识点ID列表不能为空");
            }
            
            // 将知识点ID转换为知识点名称
            List<String> kpNames = new ArrayList<>();
            for (String kpId : kpIds) {
                try {
                    KnowledgePoint kp = knowledgePointService.getKnowledgePointById(kpId);
                    if (kp != null) {
                        kpNames.add(kp.getName());
                    }
                } catch (Exception e) {
                    // 忽略找不到的知识点
                }
            }
            
            if (kpNames.isEmpty()) {
                throw new IllegalArgumentException("未找到有效的知识点");
            }
            
            List<Question> filteredQuestions = allQuestions.stream()
                    .filter(q -> Question.containsKnowledgePoint(q.getKnowledgePoints(), kpNames))
                    .collect(Collectors.toList());
            
            // 校验题目数量
            if (filteredQuestions.size() < requestDTO.getTotalCount()) {
                throw new IllegalArgumentException("符合知识点条件的题目不足，最多可选 " + filteredQuestions.size() + " 道题");
            }
            Collections.shuffle(filteredQuestions);
            selected = filteredQuestions.stream().limit(requestDTO.getTotalCount()).collect(Collectors.toList());
            
        } else if ("difficulty".equalsIgnoreCase(mode)) {
            GeneratePaperRequestDTO.DifficultyDistribution dist = requestDTO.getDifficultyDistribution();
            if (dist == null) {
                throw new IllegalArgumentException("按难度组卷时，难度分布不能为空");
            }
            
            // 校验难度分布总数是否等于要求的总题数
            int totalRequired = (dist.getEasy() != null ? dist.getEasy() : 0) +
                               (dist.getMedium() != null ? dist.getMedium() : 0) +
                               (dist.getHard() != null ? dist.getHard() : 0);
            if (totalRequired != requestDTO.getTotalCount()) {
                throw new IllegalArgumentException("难度分布总数(" + totalRequired + ")与要求题目总数(" + requestDTO.getTotalCount() + ")不匹配");
            }
            
            // 按难度级别筛选题目（使用字符串比较）
            List<Question> easy = allQuestions.stream()
                    .filter(q -> "EASY".equalsIgnoreCase(q.getDifficultyLevel()))
                    .collect(Collectors.toList());
            List<Question> medium = allQuestions.stream()
                    .filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficultyLevel()))
                    .collect(Collectors.toList());
            List<Question> hard = allQuestions.stream()
                    .filter(q -> "HARD".equalsIgnoreCase(q.getDifficultyLevel()))
                    .collect(Collectors.toList());
            
            // 校验各难度题目数量是否足够
            if (dist.getEasy() != null && easy.size() < dist.getEasy()) {
                throw new IllegalArgumentException("简单题目不足，最多可选 " + easy.size() + " 道");
            }
            if (dist.getMedium() != null && medium.size() < dist.getMedium()) {
                throw new IllegalArgumentException("中等题目不足，最多可选 " + medium.size() + " 道");
            }
            if (dist.getHard() != null && hard.size() < dist.getHard()) {
                throw new IllegalArgumentException("困难题目不足，最多可选 " + hard.size() + " 道");
            }
            
            // 随机选择各难度题目
            Collections.shuffle(easy); Collections.shuffle(medium); Collections.shuffle(hard);
            if (dist.getEasy() != null && dist.getEasy() > 0) {
                selected.addAll(easy.stream().limit(dist.getEasy()).collect(Collectors.toList()));
            }
            if (dist.getMedium() != null && dist.getMedium() > 0) {
                selected.addAll(medium.stream().limit(dist.getMedium()).collect(Collectors.toList()));
            }
            if (dist.getHard() != null && dist.getHard() > 0) {
                selected.addAll(hard.stream().limit(dist.getHard()).collect(Collectors.toList()));
            }
        } else {
            throw new IllegalArgumentException("不支持的组卷模式: " + mode);
        }
        
        System.out.println("DEBUG: Question selection completed, selected count: " + selected.size());
        
        // 组装TestPaper对象
        TestPaper paper = new TestPaper();
        paper.setPaperId(UUID.randomUUID().toString());
        paper.setCourseId(requestDTO.getCourseId());
        // 修改：直接存储完整的题目对象，而不是题目ID
        paper.setQuestions(selected);
        paper.setTotalScore((float)selected.stream().mapToDouble(Question::getScore).sum());
        
        System.out.println("DEBUG: Created TestPaper object with ID: " + paper.getPaperId());
        
        // 设置试卷标题，如果DTO中有标题则使用，否则使用默认标题
        if (requestDTO.getTitle() != null && !requestDTO.getTitle().trim().isEmpty()) {
            paper.setTitle(requestDTO.getTitle());
        } else {
            paper.setTitle("智能组卷试卷");
        }
        
        // 设置时间限制
        paper.setTimeLimit(requestDTO.getTimeLimit());
        
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        
        // 设置组卷方式
        if ("random".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.RANDOM);
        } else if ("knowledge".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.BY_KNOWLEDGE);
        } else if ("difficulty".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.DIFFICULTY_BALANCE);
        }

        // 直接保存到数据库并返回
        System.out.println("DEBUG: About to insert paper: " + paper);
        System.out.println("DEBUG: Paper ID: " + paper.getPaperId());
        System.out.println("DEBUG: Paper questions: " + paper.getQuestions());
        
        try {
            int insertResult = testPaperMapper.insert(paper);
            System.out.println("DEBUG: Insert result: " + insertResult);
            System.out.println("DEBUG: Paper inserted successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to insert paper: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return paper;
    }

    @Override
    public TestPaper savePaper(TestPaper testPaper) {
        if (testPaper.getPaperId() == null) {
            testPaper.setPaperId(UUID.randomUUID().toString());
        }
        if (testPaper.getCreatedAt() == null) {
            testPaper.setCreatedAt(LocalDateTime.now());
        }
        testPaper.setUpdatedAt(LocalDateTime.now());
        
        testPaperMapper.insert(testPaper);
        return testPaper;
    }

    @Override
    public TestPaper getPaperById(String paperId) {
        return testPaperMapper.selectById(paperId);
    }

    @Override
    public List<TestPaper> getPapersByCourseId(String courseId) {
        return testPaperMapper.selectByCourseId(courseId);
    }

    @Override
    public TestPaper getPaperByTaskId(String taskId) {
        return testPaperMapper.selectByTaskId(taskId);
    }

    @Override
    public TestPaper updatePaper(TestPaper testPaper) {
        testPaper.setUpdatedAt(LocalDateTime.now());
        testPaperMapper.update(testPaper);
        return testPaper;
    }

    @Override
    public boolean deletePaper(String paperId) {
        return testPaperMapper.deleteById(paperId) > 0;
    }

    @Override
    public List<TestPaper> getPapersByPage(String courseId, int page, int size) {
        int offset = (page - 1) * size;
        return testPaperMapper.selectByPage(courseId, offset, size);
    }

    @Override
    public int countPapers(String courseId) {
        return testPaperMapper.countByCondition(courseId);
    }

    @Override
    public Map<String, Object> getPaperWithQuestions(String paperId) {

        TestPaper testPaper = testPaperMapper.selectById(paperId);
        if (testPaper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }

        // 现在题目已经完整存储在试卷中，直接获取即可
        List<Question> questions = testPaper.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("试卷中没有题目");
        }
        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("paper", testPaper);
        result.put("questions", questions);

        return result;
    }
}