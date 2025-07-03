package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VideoController {

    @Value("${file.storage.location}")
    private String storageLocation;
    
    private final ResourceMapper resourceMapper;

    /**
     * 根据资源ID获取视频文件
     */
    @GetMapping("/video/{resourceId}")
    public ResponseEntity<org.springframework.core.io.Resource> getVideo(@PathVariable String resourceId) {
        try {
            // 根据资源ID查找资源信息
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // 确保是视频类型的资源
            if (!"video".equals(resource.getType().toString().toLowerCase())) {
                return ResponseEntity.badRequest().build();
            }

            // 构建文件路径 - 统一路径处理
            String resourceUrl = resource.getUrl();
            Path filePath;
            
            System.out.println("Resource URL from DB: " + resourceUrl);
            System.out.println("Storage Location: " + storageLocation);
            
            if (resourceUrl.startsWith("/")) {
                // 新格式路径：/videos/xxx.mp4 -> storageLocation + /videos/xxx.mp4
                // storageLocation 已经是 D:/SmartCourse/uploads，所以直接拼接
                filePath = Paths.get(storageLocation + resourceUrl);
            } else {
                // 旧格式路径：直接拼接到storageLocation
                filePath = Paths.get(storageLocation).resolve(resourceUrl);
            }
            
            System.out.println("Final file path: " + filePath.toString());
            System.out.println("File exists: " + Files.exists(filePath));
            
            UrlResource fileResource = new UrlResource(filePath.toUri());

            if (fileResource.exists() && fileResource.isReadable()) {
                String contentType = determineVideoContentType(resource.getUrl());
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResource.getFilename() + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(fileResource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 兼容性接口：/api/videos/{resourceId}
     */
    @GetMapping("/videos/{resourceId}")
    public ResponseEntity<org.springframework.core.io.Resource> getVideoAlternative(@PathVariable String resourceId) {
        return getVideo(resourceId);
    }

    /**
     * 调试接口：查看资源详细信息
     */
    @GetMapping("/debug/resource/{resourceId}")
    public ResponseEntity<?> debugResource(@PathVariable String resourceId) {
        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.ok("Resource not found in database");
            }

            Map<String, Object> debug = new HashMap<>();
            debug.put("resourceId", resource.getResourceId());
            debug.put("name", resource.getName());
            debug.put("type", resource.getType());
            debug.put("url", resource.getUrl());
            debug.put("size", resource.getSize());
            debug.put("uploadTime", resource.getUploadTime());

            // 检查文件路径构建
            String resourceUrl = resource.getUrl();
            Path filePath;
            
            if (resourceUrl.startsWith("/")) {
                filePath = Paths.get(storageLocation + resourceUrl);
            } else {
                filePath = Paths.get(storageLocation).resolve(resourceUrl);
            }
            
            debug.put("constructedPath", filePath.toString());
            debug.put("fileExists", Files.exists(filePath));
            debug.put("storageLocation", storageLocation);

            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    /**
     * 调试接口：获取所有资源列表
     */
    @GetMapping("/debug/resources")
    public ResponseEntity<?> debugAllResources() {
        try {
            // 这里我们需要手动实现一个简单的查询，因为ResourceMapper可能没有这个方法
            return ResponseEntity.ok("请使用 /debug/resource/{resourceId} 查询具体资源");
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }



    private String determineVideoContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "wmv": return "video/x-ms-wmv";
            case "flv": return "video/x-flv";
            case "webm": return "video/webm";
            case "mkv": return "video/x-matroska";
            default: return "video/mp4"; // 默认为mp4
        }
    }
}
