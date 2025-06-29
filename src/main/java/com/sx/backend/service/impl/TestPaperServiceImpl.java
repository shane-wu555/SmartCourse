package com.sx.backend.service.impl;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.entity.PaperGenerationMethod;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.service.QuestionService;
import com.sx.backend.service.TestPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestPaperServiceImpl implements TestPaperService {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public TestPaper generatePaper(GeneratePaperRequestDTO requestDTO) {
        List<Question> allQuestions = questionService.getQuestionsByBankId(requestDTO.getCourseId());
        List<Question> selected = new ArrayList<>();
        String mode = requestDTO.getMode();
        if ("random".equalsIgnoreCase(mode)) {
            Collections.shuffle(allQuestions);
            selected = allQuestions.stream().limit(requestDTO.getTotalCount()).collect(Collectors.toList());
        } else if ("knowledge".equalsIgnoreCase(mode)) {
            List<String> kpIds = requestDTO.getKnowledgePointIds();
            selected = allQuestions.stream()
                    .filter(q -> Question.containsKnowledgePoint(q.getKnowledgePoints(), kpIds))
                    .limit(requestDTO.getTotalCount())
                    .collect(Collectors.toList());
        } else if ("type".equalsIgnoreCase(mode)) {
            List<String> types = requestDTO.getQuestionTypes();
            selected = allQuestions.stream()
                    .filter(q -> types != null && types.contains(q.getTypeString()))
                    .limit(requestDTO.getTotalCount())
                    .collect(Collectors.toList());
        } else if ("difficulty".equalsIgnoreCase(mode)) {
            GeneratePaperRequestDTO.DifficultyDistribution dist = requestDTO.getDifficultyDistribution();
            if (dist != null) {
                List<Question> easy = allQuestions.stream().filter(q -> "easy".equalsIgnoreCase(q.getDifficultyLevel())).collect(Collectors.toList());
                List<Question> medium = allQuestions.stream().filter(q -> "medium".equalsIgnoreCase(q.getDifficultyLevel())).collect(Collectors.toList());
                List<Question> hard = allQuestions.stream().filter(q -> "hard".equalsIgnoreCase(q.getDifficultyLevel())).collect(Collectors.toList());
                Collections.shuffle(easy); Collections.shuffle(medium); Collections.shuffle(hard);
                if (dist.getEasy() != null) selected.addAll(easy.stream().limit(dist.getEasy()).collect(Collectors.toList()));
                if (dist.getMedium() != null) selected.addAll(medium.stream().limit(dist.getMedium()).collect(Collectors.toList()));
                if (dist.getHard() != null) selected.addAll(hard.stream().limit(dist.getHard()).collect(Collectors.toList()));
            }
        }
        // 组装TestPaper对象
        TestPaper paper = new TestPaper();
        paper.setCourseId(requestDTO.getCourseId());
        paper.setQuestions(selected.stream().map(Question::getQuestionId).collect(Collectors.toList()));
        paper.setTotalScore((float)selected.stream().mapToDouble(Question::getScore).sum());
        paper.setTitle("智能组卷试卷");
        if ("random".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.RANDOM);
        } else if ("knowledge".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.BY_KNOWLEDGE);
        } else if ("difficulty".equalsIgnoreCase(mode)) {
            paper.setGenerationMethod(PaperGenerationMethod.DIFFICULTY_BALANCE);
        }

        return paper;
    }
}
