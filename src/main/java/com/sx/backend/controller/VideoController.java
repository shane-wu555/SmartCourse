package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

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

            // 构建文件路径
            Path filePath = Paths.get(storageLocation).resolve(resource.getUrl());
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
