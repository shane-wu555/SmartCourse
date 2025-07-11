package com.sx.backend.service.impl;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.dto.GeneratePaperRequestDTO.DifficultyDistribution;
import com.sx.backend.entity.*;
import com.sx.backend.mapper.TestPaperMapper;
import com.sx.backend.service.KnowledgePointService;
import com.sx.backend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPaperServiceImplTest {

    @InjectMocks
    private TestPaperServiceImpl testPaperService;

    @Mock
    private QuestionService questionService;

    @Mock
    private KnowledgePointService knowledgePointService;

    @Mock
    private TestPaperMapper testPaperMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper to create sample questions
    private Question createQuestion(String id, DifficultyLevel difficulty, List<String> knowledgePoints, Float score) {
        Question q = new Question();
        q.setQuestionId(id);
        q.setDifficultylevel(difficulty);
        q.setKnowledgePoints(knowledgePoints);
        q.setScore(score);
        return q;
    }

    // 模拟 Question.containsKnowledgePoint 静态方法，这里用Mockito的spy或替代实现
    // 由于静态方法不好mock，假设它的逻辑是知识点列表中包含任何kpName即返回true
    // 实际测试环境可使用 PowerMockito 或改造代码
    private static boolean containsKnowledgePoint(List<String> questionKps, List<String> targetKps) {
        for (String kp : targetKps) {
            if (questionKps.contains(kp)) return true;
        }
        return false;
    }

    @Test
    void generatePaper_shouldThrowWhenBankIdIsNull() {
        GeneratePaperRequestDTO dto = new GeneratePaperRequestDTO();
        dto.setBankId(null);
        dto.setMode("random");
        dto.setTotalCount(1);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> testPaperService.generatePaper(dto));
        assertEquals("题库ID不能为空", e.getMessage());
    }

    @Test
    void generatePaper_randomMode_success() {
        GeneratePaperRequestDTO dto = new GeneratePaperRequestDTO();
        dto.setBankId("bank1");
        dto.setMode("random");
        dto.setTotalCount(2);

        List<Question> questions = Arrays.asList(
                createQuestion("q1", DifficultyLevel.EASY, Collections.emptyList(), 5f),
                createQuestion("q2", DifficultyLevel.MEDIUM, Collections.emptyList(), 7f),
                createQuestion("q3", DifficultyLevel.HARD, Collections.emptyList(), 10f)
        );
        when(questionService.getQuestionsByBankId("bank1")).thenReturn(questions);
        when(testPaperMapper.insert(any())).thenReturn(1);

        TestPaper paper = testPaperService.generatePaper(dto);

        assertNotNull(paper.getPaperId());
        assertEquals(2, paper.getQuestions().size());
        assertEquals(PaperGenerationMethod.RANDOM, paper.getGenerationMethod());

        // 分数计算正确
        double totalScore = paper.getQuestions().stream().mapToDouble(Question::getScore).sum();
        assertEquals((float) totalScore, paper.getTotalScore());

        verify(testPaperMapper).insert(any());
    }

    @Test
    void generatePaper_knowledgeMode_success() {
        GeneratePaperRequestDTO dto = new GeneratePaperRequestDTO();
        dto.setBankId("bank1");
        dto.setMode("knowledge");
        dto.setTotalCount(1);
        dto.setKnowledgePointIds(Arrays.asList("kp1"));

        List<Question> allQuestions = Arrays.asList(
                createQuestion("q1", DifficultyLevel.EASY, Arrays.asList("Math", "Physics"), 5f),
                createQuestion("q2", DifficultyLevel.MEDIUM, Arrays.asList("English"), 7f)
        );
        when(questionService.getQuestionsByBankId("bank1")).thenReturn(allQuestions);

        KnowledgePoint kp = new KnowledgePoint();
        kp.setPointId("kp1");
        kp.setName("Math");
        when(knowledgePointService.getKnowledgePointById("kp1")).thenReturn(kp);

        when(testPaperMapper.insert(any())).thenReturn(1);

        // 模拟Question.containsKnowledgePoint静态方法的逻辑：Math知识点包含q1
        // 这里我们在service内部调用的是静态方法，我们无法直接mock静态方法，实际可用PowerMockito等
        // 但因这里简单演示，只要q1的知识点包含kp的name，逻辑是生效的。

        TestPaper paper = testPaperService.generatePaper(dto);
        assertEquals(PaperGenerationMethod.BY_KNOWLEDGE, paper.getGenerationMethod());
        assertEquals(1, paper.getQuestions().size());
        assertTrue(paper.getQuestions().get(0).getKnowledgePoints().contains("Math"));
        verify(testPaperMapper).insert(any());
    }

    @Test
    void generatePaper_difficultyMode_success() {
        GeneratePaperRequestDTO dto = new GeneratePaperRequestDTO();
        dto.setBankId("bank1");
        dto.setMode("difficulty");
        dto.setTotalCount(3);

        DifficultyDistribution dist = new DifficultyDistribution();
        dist.setEasy(1);
        dist.setMedium(1);
        dist.setHard(1);
        dto.setDifficultyDistribution(dist);

        List<Question> allQuestions = Arrays.asList(
                createQuestion("q1", DifficultyLevel.EASY, Collections.emptyList(), 5f),
                createQuestion("q2", DifficultyLevel.MEDIUM, Collections.emptyList(), 7f),
                createQuestion("q3", DifficultyLevel.HARD, Collections.emptyList(), 10f),
                createQuestion("q4", DifficultyLevel.EASY, Collections.emptyList(), 3f)
        );
        when(questionService.getQuestionsByBankId("bank1")).thenReturn(allQuestions);
        when(testPaperMapper.insert(any())).thenReturn(1);

        TestPaper paper = testPaperService.generatePaper(dto);
        assertEquals(PaperGenerationMethod.DIFFICULTY_BALANCE, paper.getGenerationMethod());
        assertEquals(3, paper.getQuestions().size());

        // 各难度题目数量符合分布
        long easyCount = paper.getQuestions().stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficultyLevel())).count();
        long mediumCount = paper.getQuestions().stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficultyLevel())).count();
        long hardCount = paper.getQuestions().stream().filter(q -> "HARD".equalsIgnoreCase(q.getDifficultyLevel())).count();

        assertEquals(1, easyCount);
        assertEquals(1, mediumCount);
        assertEquals(1, hardCount);

        verify(testPaperMapper).insert(any());
    }

    @Test
    void generatePaper_unsupportedMode_shouldThrow() {
        GeneratePaperRequestDTO dto = new GeneratePaperRequestDTO();
        dto.setBankId("bank1");
        dto.setMode("unknown");
        dto.setTotalCount(1);

        when(questionService.getQuestionsByBankId("bank1")).thenReturn(Collections.emptyList());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> testPaperService.generatePaper(dto));
        assertTrue(e.getMessage().contains("不支持的组卷模式"));
    }
}
