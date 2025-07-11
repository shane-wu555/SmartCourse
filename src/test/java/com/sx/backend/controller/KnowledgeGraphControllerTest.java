package com.sx.backend.controller;

import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.service.KnowledgeGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgeGraphControllerTest {

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    @InjectMocks
    private com.sx.backend.controller.KnowledgeGraphController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateRelationsForCourse_success() throws Exception {
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        when(knowledgeGraphService.generateGraphForCourse("1")).thenReturn(dto);

        ResponseEntity<KnowledgeGraphDTO> response = controller.generateRelationsForCourse("1");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGenerateRelationsForCourse_exception() throws Exception {
        when(knowledgeGraphService.generateGraphForCourse("1")).thenThrow(new RuntimeException("error"));

        ResponseEntity<KnowledgeGraphDTO> response = controller.generateRelationsForCourse("1");
        assertEquals(500, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetKnowledgeGraphByCourse_success() {
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        when(knowledgeGraphService.getKnowledgeGraphFromDatabase("2")).thenReturn(dto);

        ResponseEntity<KnowledgeGraphDTO> response = controller.getKnowledgeGraphByCourse("2");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGenerate_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        when(knowledgeGraphService.generateGraph(anyString())).thenReturn(dto);

        ResponseEntity<KnowledgeGraphDTO> response = controller.generate(file);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGenerateByText_success() throws Exception {
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        when(knowledgeGraphService.generateGraph("text")).thenReturn(dto);

        ResponseEntity<KnowledgeGraphDTO> response = controller.generateByText("text");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testTestAIGeneration_success() throws Exception {
        doNothing().when(knowledgeGraphService).testAIGeneration("3");

        ResponseEntity<String> response = controller.testAIGeneration("3");
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("测试完成"));
    }

    @Test
    void testTestAIGeneration_exception() throws Exception {
        doThrow(new RuntimeException("fail")).when(knowledgeGraphService).testAIGeneration("3");

        ResponseEntity<String> response = controller.testAIGeneration("3");
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("测试失败"));
    }
}