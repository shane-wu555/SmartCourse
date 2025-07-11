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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceAccessControllerTest {

    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private PreviewService previewService;

    @InjectMocks
    private ResourceAccessController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Resource buildResource(String id, ResourceType type, String url, String mimeType) {
        Resource r = new Resource();
        r.setResourceId(id);
        r.setType(type);
        r.setUrl(url);
        r.setMimeType(mimeType);
        return r;
    }

    @Test
    void testAccessResource_documentType() throws OfficeException, IOException {
        Resource resource = buildResource("1", ResourceType.DOCUMENT, "doc/test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        when(previewService.generatePreview(resource, resource.getMimeType())).thenReturn("/preview/doc1.pdf");

        ResponseEntity<?> resp = controller.accessResource("1");
        assertEquals(302, resp.getStatusCodeValue());
        assertEquals("/preview/doc1.pdf", resp.getHeaders().getFirst("Location"));
    }

    @Test
    void testAccessResource_pptType() throws OfficeException, IOException {
        Resource resource = buildResource("2", ResourceType.PPT, "ppt/test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        when(resourceMapper.getResourceById("2")).thenReturn(resource);
        when(previewService.generatePreview(resource, resource.getMimeType())).thenReturn("/preview/ppt2.pdf");

        ResponseEntity<?> resp = controller.accessResource("2");
        assertEquals(302, resp.getStatusCodeValue());
        assertEquals("/preview/ppt2.pdf", resp.getHeaders().getFirst("Location"));
    }

    @Test
    void testAccessResource_pdfType() {
        Resource resource = buildResource("3", ResourceType.PDF, "pdf/test.pdf", "application/pdf");
        when(resourceMapper.getResourceById("3")).thenReturn(resource);

        ResponseEntity<?> resp = controller.accessResource("3");
        assertEquals(302, resp.getStatusCodeValue());
        assertEquals("/uploads/pdf/test.pdf", resp.getHeaders().getFirst("Location"));
    }

    @Test
    void testAccessResource_imageType() {
        Resource resource = buildResource("4", ResourceType.IMAGE, "/images/img1.png", "image/png");
        when(resourceMapper.getResourceById("4")).thenReturn(resource);

        ResponseEntity<?> resp = controller.accessResource("4");
        assertEquals(302, resp.getStatusCodeValue());
        assertEquals("/uploads/images/img1.png", resp.getHeaders().getFirst("Location"));
    }

    @Test
    void testAccessResource_notFound() {
        when(resourceMapper.getResourceById("6")).thenReturn(null);

        ResponseEntity<?> resp = controller.accessResource("6");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testAccessResource_exception() {
        when(resourceMapper.getResourceById("7")).thenThrow(new RuntimeException("db error"));

        ResponseEntity<?> resp = controller.accessResource("7");
        assertEquals(500, resp.getStatusCodeValue());
        assertTrue(resp.getBody().toString().contains("访问资源失败"));
    }
}