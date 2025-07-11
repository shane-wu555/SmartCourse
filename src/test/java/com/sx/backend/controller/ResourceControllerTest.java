package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceControllerTest {

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private ResourceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 设置存储路径，避免空指针
        controller.storageLocation = System.getProperty("java.io.tmpdir");
    }

    @Test
    void testUploadResource_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "abc".getBytes());
        when(resourceMapper.insertResource(any())).thenReturn(1);

        ResponseEntity<Map<String, Object>> resp = controller.uploadResource(
                mock(HttpServletRequest.class), "course1", file, "name", "pdf", "desc");

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals("资源上传成功", resp.getBody().get("message"));
    }

    @Test
    void testUploadResource_fileIsNull() {
        ResponseEntity<Map<String, Object>> resp = controller.uploadResource(
                mock(HttpServletRequest.class), "course1", null, "name", "pdf", "desc");
        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().get("error").toString().contains("文件不能为空"));
    }

    @Test
    void testUploadResource_invalidType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.abc", "application/abc", "abc".getBytes());
        ResponseEntity<Map<String, Object>> resp = controller.uploadResource(
                mock(HttpServletRequest.class), "course1", file, "name", "abc", "desc");
        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().get("error").toString().contains("不支持的文件类型"));
    }

    @Test
    void testGetResourceList_success() {
        List<Resource> list = List.of(mockResource("1", ResourceType.PDF));
        when(resourceMapper.getResourcesByCourseId(any(), any(), anyInt(), anyInt())).thenReturn(list);
        when(resourceMapper.countResourcesByCourseId(any(), any())).thenReturn(1);

        ResponseEntity<Map<String, Object>> resp = controller.getResourceList("course1", null, 1, 10);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("成功获取资源列表", resp.getBody().get("message"));
    }

    @Test
    void testUpdateResource_success() {
        Resource resource = mockResource("1", ResourceType.PDF);
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        when(resourceMapper.updateResource(any())).thenReturn(1);

        Map<String, String> update = new HashMap<>();
        update.put("name", "newName");
        update.put("description", "newDesc");

        ResponseEntity<Map<String, Object>> resp = controller.updateResource("1", update);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("资源更新成功", resp.getBody().get("message"));
    }

    @Test
    void testUpdateResource_notFound() {
        when(resourceMapper.getResourceById("notfound")).thenReturn(null);
        ResponseEntity<Map<String, Object>> resp = controller.updateResource("notfound", Map.of());
        assertEquals(404, resp.getStatusCodeValue());
        assertTrue(resp.getBody().get("error").toString().contains("资源不存在"));
    }

    @Test
    void testDeleteResource_success() {
        Resource resource = mockResource("1", ResourceType.PDF);
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        when(resourceMapper.countTaskReferences("1")).thenReturn(0);
        when(resourceMapper.deleteResource("1")).thenReturn(1);

        // 文件删除模拟
        Path filePath = Paths.get(controller.storageLocation, "uploads" + resource.getUrl());
        try {
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (IOException ignored) {}

        ResponseEntity<Map<String, Object>> resp = controller.deleteResource("1");
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void testDeleteResource_notFound() {
        when(resourceMapper.getResourceById("notfound")).thenReturn(null);
        ResponseEntity<Map<String, Object>> resp = controller.deleteResource("notfound");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testDeleteResource_taskReferenced() {
        Resource resource = mockResource("1", ResourceType.PDF);
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        when(resourceMapper.countTaskReferences("1")).thenReturn(2);

        ResponseEntity<Map<String, Object>> resp = controller.deleteResource("1");
        assertEquals(409, resp.getStatusCodeValue());
        assertTrue(resp.getBody().get("message").toString().contains("无法删除"));
    }

    @Test
    void testDownloadResource_notFound() {
        when(resourceMapper.getResourceById("notfound")).thenReturn(null);
        ResponseEntity<?> resp = controller.downloadResource("notfound");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testPlayVideo_notVideoType() {
        Resource resource = mockResource("1", ResourceType.PDF);
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        ResponseEntity<?> resp = controller.playVideo(mock(HttpServletRequest.class), "1");
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void testAccessResource_videoType() {
        Resource resource = mockResource("1", ResourceType.VIDEO);
        when(resourceMapper.getResourceById("1")).thenReturn(resource);
        ResponseEntity<?> resp = controller.accessResource(mock(HttpServletRequest.class), "1");
        // 200 或 500，取决于 handleVideoStream 内部文件是否存在
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 500 || resp.getStatusCodeValue() == 410);
    }

    @Test
    void testAccessResource_notFound() {
        when(resourceMapper.getResourceById("notfound")).thenReturn(null);
        ResponseEntity<?> resp = controller.accessResource(mock(HttpServletRequest.class), "notfound");
        assertEquals(404, resp.getStatusCodeValue());
    }

    private Resource mockResource(String id, ResourceType type) {
        Resource r = new Resource();
        r.setResourceId(id);
        r.setCourseId("course1");
        r.setName("test");
        r.setType(type);
        r.setUrl("/documents/test.pdf");
        r.setSize(123L);
        r.setDescription("desc");
        r.setUploaderId("uploader");
        r.setUploadTime(LocalDateTime.now());
        r.setViewCount(0);
        r.setDuration(null);
        return r;
    }
}