package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.service.PreviewService;
import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreviewControllerTest {

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private PreviewService previewService;

    @InjectMocks
    private PreviewController previewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPreview_found() {
        Resource resource = new Resource();
        resource.setResourceId("1");
        resource.setName("test.pdf");
        resource.setUrl("test.pdf");
        resource.setType(ResourceType.PDF);
        resource.setMimeType("application/pdf");
        resource.setSize(123L);

        when(resourceMapper.getResourceById("1")).thenReturn(resource);

        ResponseEntity<?> response = previewController.testPreview("1");
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("1", body.get("resourceId"));
        assertEquals("test.pdf", body.get("name"));
        assertEquals("http://localhost:8082/uploads/test.pdf", body.get("fullAccessUrl"));
    }

    @Test
    void testPreview_notFound() {
        when(resourceMapper.getResourceById("2")).thenReturn(null);

        ResponseEntity<?> response = previewController.testPreview("2");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testPreview_exception() {
        when(resourceMapper.getResourceById("3")).thenThrow(new RuntimeException("db error"));

        ResponseEntity<?> response = previewController.testPreview("3");
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("测试失败"));
    }

    @Test
    void testGetPreviewUrl_found() throws OfficeException, IOException {
        Resource resource = new Resource();
        resource.setResourceId("4");
        resource.setMimeType("application/pdf");

        when(resourceMapper.getResourceById("4")).thenReturn(resource);
        when(previewService.generatePreview(resource, "application/pdf")).thenReturn("preview-url");

        ResponseEntity<String> response = previewController.getPreviewUrl("4");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("preview-url", response.getBody());
    }

    @Test
    void testGetPreviewUrl_notFound() {
        when(resourceMapper.getResourceById("5")).thenReturn(null);

        ResponseEntity<String> response = previewController.getPreviewUrl("5");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetPreviewUrl_exception() {
        when(resourceMapper.getResourceById("6")).thenThrow(new RuntimeException("db error"));

        ResponseEntity<String> response = previewController.getPreviewUrl("6");
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("预览生成失败"));
    }
}