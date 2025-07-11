package com.sx.backend.controller;

import com.sx.backend.dto.RecommendationRequest;
import com.sx.backend.dto.RecommendationResponse;
import com.sx.backend.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationControllerTest {

    @InjectMocks
    private RecommendationController controller;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpServletRequest.getAttribute("userId")).thenReturn("testUser");
    }

    @Test
    void testGenerateRecommendation_success() {
        RecommendationRequest req = new RecommendationRequest();
        RecommendationResponse resp = new RecommendationResponse();
        when(recommendationService.generateRecommendation(any())).thenReturn(resp);

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.generateRecommendation(req);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("推荐生成成功", result.getBody().getMessage());
        assertEquals(resp, result.getBody().getData());
        assertEquals("testUser", req.getStudentId());
    }

    @Test
    void testGenerateRecommendation_exception() {
        RecommendationRequest req = new RecommendationRequest();
        when(recommendationService.generateRecommendation(any())).thenThrow(new RuntimeException("fail"));

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.generateRecommendation(req);

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().getMessage().contains("推荐生成失败"));
        assertNull(result.getBody().getData());
    }

    @Test
    void testGetKnowledgePointRecommendations_success() {
        RecommendationResponse resp = new RecommendationResponse();
        when(recommendationService.getKnowledgePointRecommendations("testUser", "c1", 3)).thenReturn(resp);

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getKnowledgePointRecommendations("c1", 3);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("知识点推荐获取成功", result.getBody().getMessage());
        assertEquals(resp, result.getBody().getData());
    }

    @Test
    void testGetKnowledgePointRecommendations_exception() {
        when(recommendationService.getKnowledgePointRecommendations(any(), any(), anyInt()))
                .thenThrow(new RuntimeException("fail"));

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getKnowledgePointRecommendations("c1", 3);

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().getMessage().contains("获取知识点推荐失败"));
        assertNull(result.getBody().getData());
    }

    @Test
    void testGetResourceRecommendations_success() {
        RecommendationResponse resp = new RecommendationResponse();
        when(recommendationService.getResourceRecommendations("testUser", "c2", 2)).thenReturn(resp);

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getResourceRecommendations("c2", 2);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("资源推荐获取成功", result.getBody().getMessage());
        assertEquals(resp, result.getBody().getData());
    }

    @Test
    void testGetResourceRecommendations_exception() {
        when(recommendationService.getResourceRecommendations(any(), any(), anyInt()))
                .thenThrow(new RuntimeException("fail"));

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getResourceRecommendations("c2", 2);

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().getMessage().contains("获取资源推荐失败"));
        assertNull(result.getBody().getData());
    }

    @Test
    void testGetComprehensiveRecommendations_success() {
        RecommendationResponse resp = new RecommendationResponse();
        when(recommendationService.getComprehensiveRecommendations("testUser", "c3")).thenReturn(resp);

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getComprehensiveRecommendations("c3");

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("综合推荐获取成功", result.getBody().getMessage());
        assertEquals(resp, result.getBody().getData());
    }

    @Test
    void testGetComprehensiveRecommendations_exception() {
        when(recommendationService.getComprehensiveRecommendations(any(), any()))
                .thenThrow(new RuntimeException("fail"));

        ResponseEntity<ApiResponse<RecommendationResponse>> result = controller.getComprehensiveRecommendations("c3");

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().getMessage().contains("获取综合推荐失败"));
        assertNull(result.getBody().getData());
    }
}