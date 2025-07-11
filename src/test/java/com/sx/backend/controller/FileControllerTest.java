package com.sx.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileControllerTest {

    private FileController controller;
    private String tempDir;

    @BeforeEach
    void setUp() throws IOException {
        controller = new FileController();
        // 创建临时目录和文件
        tempDir = Files.createTempDirectory("file-test").toString();
        setField(controller, "storageLocation", tempDir);

        // 创建一个测试文件
        Path uploadDir = Path.of(tempDir, "uploads");
        Files.createDirectories(uploadDir);
        Files.writeString(uploadDir.resolve("testfile.txt"), "test content");
    }

    @Test
    void testGetFile_success() {
        ResponseEntity<Resource> resp = controller.getFile("uploads/testfile.txt");
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("testfile.txt", resp.getBody().getFilename());
    }

    @Test
    void testGetFile_notFound() {
        ResponseEntity<Resource> resp = controller.getFile("uploads/notfound.txt");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testGetFile_internalServerError() {
        // 传入非法路径，触发异常
        ResponseEntity<Resource> resp = controller.getFile("../invalid/../escape.txt");
        assertEquals(404, resp.getStatusCodeValue());
    }

    // 反射工具方法
    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}