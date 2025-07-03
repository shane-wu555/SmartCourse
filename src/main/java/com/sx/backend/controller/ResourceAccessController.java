package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.service.PreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/resources")
public class ResourceAccessController {

    @Value("${file.storage.location}")
    private String storageLocation;

    private final ResourceMapper resourceMapper;
    private final PreviewService previewService;

    @Autowired
    public ResourceAccessController(ResourceMapper resourceMapper, PreviewService previewService) {
        this.resourceMapper = resourceMapper;
        this.previewService = previewService;
    }

    /**
     * 处理资源访问请求
     * 对于文档类型（如docx），会自动转换为PDF进行预览
     */
    @GetMapping("/{resourceId}")
    public ResponseEntity<?> accessResource(@PathVariable String resourceId) {
        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // 根据资源类型决定处理方式
            switch (resource.getType()) {
                case DOCUMENT:
                case PPT:
                    // 文档和PPT类型需要转换为PDF预览
                    String previewUrl = previewService.generatePreview(resource, resource.getMimeType());
                    // 重定向到预览URL
                    return ResponseEntity.status(302)
                            .header("Location", previewUrl)
                            .build();
                    
                case PDF:
                case IMAGE:
                case VIDEO:
                case AUDIO:
                    // 其他类型直接提供原文件
                    return serveFile(resource.getUrl());
                    
                default:
                    return ResponseEntity.badRequest()
                            .body("不支持的文件类型: " + resource.getType());
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("访问资源失败: " + e.getMessage());
        }
    }

    /**
     * 提供文件服务
     */
    private ResponseEntity<?> serveFile(String relativePath) throws IOException {
        // 统一处理资源路径
        String webPath;
        if (relativePath.startsWith("/")) {
            // 新格式路径：/videos/xxx.mp4 -> /uploads/videos/xxx.mp4
            webPath = "/uploads" + relativePath;
        } else {
            // 旧格式路径：直接添加 /uploads/ 前缀
            webPath = "/uploads/" + relativePath;
        }
        
        // 重定向到静态资源路径
        return ResponseEntity.status(302)
                .header("Location", webPath)
                .build();
    }
}
