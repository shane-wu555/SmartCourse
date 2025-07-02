package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.service.PreviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/preview")
public class PreviewController {
    
    private static final Logger log = LoggerFactory.getLogger(PreviewController.class);

    private final ResourceMapper resourceMapper;
    private final PreviewService previewService;

    @Autowired
    public PreviewController(ResourceMapper resourceMapper, PreviewService previewService) {
        this.resourceMapper = resourceMapper;
        this.previewService = previewService;
    }

    @GetMapping("/test/{resourceId}")
    public ResponseEntity<?> testPreview(@PathVariable String resourceId) {
        try {
            log.info("测试预览接口，资源ID: {}", resourceId);

            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("找到资源: {}", resource);

            // 返回详细的调试信息
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("resourceId", resource.getResourceId());
            debugInfo.put("name", resource.getName());
            debugInfo.put("url", resource.getUrl());
            debugInfo.put("type", resource.getType());
            debugInfo.put("mimeType", resource.getMimeType());
            debugInfo.put("size", resource.getSize());

            // 构建完整的文件访问URL
            String fullUrl = "http://localhost:8082/uploads/" + resource.getUrl();
            debugInfo.put("fullAccessUrl", fullUrl);

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            log.error("测试预览失败", e);
            return ResponseEntity.internalServerError()
                    .body("测试失败: " + e.getMessage());
        }
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<String> getPreviewUrl(@PathVariable String resourceId) {
        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            String previewUrl = previewService.generatePreview(resource, resource.getMimeType());
            return ResponseEntity.ok(previewUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("预览生成失败: " + e.getMessage());
        }
    }
}