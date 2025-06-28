package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.service.PreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preview")
public class PreviewController {

    private final ResourceMapper resourceMapper;
    private final PreviewService previewService;

    @Autowired
    public PreviewController(ResourceMapper resourceMapper, PreviewService previewService) {
        this.resourceMapper = resourceMapper;
        this.previewService = previewService;
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<String> getPreviewUrl(@PathVariable String resourceId) {
        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            String previewUrl = previewService.generatePreview(resource);
            return ResponseEntity.ok(previewUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("预览生成失败: " + e.getMessage());
        }
    }
}