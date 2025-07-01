package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentResourceController {

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${python.executable:python}")
    private String pythonExecutable;

    private final ResourceMapper resourceMapper;
    private Path pythonScriptPath;

    public StudentResourceController(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
        this.pythonScriptPath = extractPythonScript();
    }

    // === 获取资源列表接口 ===
    @GetMapping("/courses/{courseId}/resources")
    public ResponseEntity<Map<String, Object>> getResourceList(
            @PathVariable String courseId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        int offset = (page - 1) * size;

        try {
            List<Resource> resources = resourceMapper.getResourcesByCourseId(
                    courseId, type, offset, size);

            int total = resourceMapper.countResourcesByCourseId(courseId, type);
            int totalPages = (int) Math.ceil((double) total / size);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功获取资源列表");

            Map<String, Object> data = new HashMap<>();
            data.put("page", page);
            data.put("size", size);
            data.put("totalElements", total);
            data.put("totalPages", totalPages);

            List<Map<String, Object>> content = new ArrayList<>();
            for (Resource resource : resources) {
                content.add(createResourceResponse(resource));
            }
            data.put("content", content);

            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "获取资源列表失败: " + e.getMessage()));
        }
    }

    // === 获取资源详情接口 ===
    @GetMapping("/resources/{resourceId}")
    public ResponseEntity<Map<String, Object>> getResourceDetail(
            @PathVariable String resourceId) {

        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.status(404)
                        .body(errorResponse(404, "资源不存在"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功获取资源详情");
            response.put("data", createResourceResponse(resource));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "获取资源详情失败: " + e.getMessage()));
        }
    }

    // === 更新资源接口 ===
    @PutMapping("/resources/{resourceId}")
    public ResponseEntity<Map<String, Object>> updateResource(
            @PathVariable String resourceId,
            @RequestBody Map<String, String> updateData) {

        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.status(404)
                        .body(errorResponse(404, "资源不存在"));
            }

            if (updateData.containsKey("name")) {
                resource.setName(updateData.get("name"));
            }
            if (updateData.containsKey("description")) {
                resource.setDescription(updateData.get("description"));
            }

            int result = resourceMapper.updateResource(resource);
            if (result == 0) {
                return ResponseEntity.internalServerError()
                        .body(errorResponse(500, "资源更新失败"));
            }

            Resource updatedResource = resourceMapper.getResourceById(resourceId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "资源更新成功");
            response.put("data", createResourceResponse(updatedResource));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(errorResponse(400, "无效的资源类型"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "更新资源失败: " + e.getMessage()));
        }
    }

    // === 删除资源接口 ===
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<Map<String, Object>> deleteResource(
            @PathVariable String resourceId) {

        try {
            Resource resource = resourceMapper.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.status(404)
                        .body(errorResponse(404, "资源不存在"));
            }

            int taskCount = resourceMapper.countTaskReferences(resourceId);
            if (taskCount > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 409);
                response.put("message", "资源被任务引用，无法删除");

                Map<String, Object> details = new HashMap<>();
                details.put("taskCount", taskCount);
                response.put("details", details);

                return ResponseEntity.status(409).body(response);
            }

            Path filePath = Paths.get(resource.getUrl());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            int result = resourceMapper.deleteResource(resourceId);
            if (result == 0) {
                return ResponseEntity.internalServerError()
                        .body(errorResponse(500, "资源删除失败"));
            }

            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "文件删除失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "删除资源失败: " + e.getMessage()));
        }
    }

    // === 下载资源接口 ===
    @GetMapping("/resources/{resourceId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(
            @PathVariable String resourceId) {

        try {
            Resource dbResource = resourceMapper.getResourceById(resourceId);
            if (dbResource == null) {
                return ResponseEntity.status(404).build();
            }

            // 修复：使用配置的存储位置而不是绝对路径
            Path storageRoot = Paths.get(storageLocation);
            Path filePath = storageRoot.resolve(dbResource.getUrl().replaceFirst("^/", ""));

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(410).build();
            }

            // 设置下载响应头 - 使用RFC 5987编码解决中文问题
            HttpHeaders headers = new HttpHeaders();
            String filename = getDownloadFilename(dbResource);

            // 对文件名进行URL编码（UTF-8），并替换+为%20
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            // 构建符合RFC 5987规范的Content-Disposition头
            String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"; " +
                    "filename*=UTF-8''" + encodedFilename;

            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

            // 根据文件类型设置Content-Type
            String contentType = getContentType(dbResource.getType());
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 返回文件流
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(filePath))
                    .body(new FileSystemResource(filePath));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 辅助方法：生成下载文件名
    private String getDownloadFilename(Resource resource) {
        String extension = getFileExtension(resource.getUrl());
        return resource.getName() + (extension.isEmpty() ? "" : "." + extension);
    }

    // 辅助方法：根据资源类型获取Content-Type
    private String getContentType(ResourceType type) {
        switch (type) {
            case PDF: return "application/pdf";
            case PPT: return "application/vnd.ms-powerpoint";
            case VIDEO: return "video/mp4";
            case DOCUMENT: return "application/msword";
            default: return "application/octet-stream";
        }
    }

    // === 辅助方法 ===
    private Path extractPythonScript() {
        try {
            org.springframework.core.io.Resource resource =
                    new ClassPathResource("scripts/video_duration.py");

            Path tempFile = Files.createTempFile("video_duration_", ".py");
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract Python script", e);
        }
    }

    private Map<String, Object> errorResponse(int code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("error", message);
        return error;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private Map<String, Object> createResourceResponse(Resource resource) {
        Map<String, Object> data = new HashMap<>();
        data.put("resourceId", resource.getResourceId());
        data.put("courseId", resource.getCourseId());
        data.put("name", resource.getName());
        data.put("type", resource.getType().name());
        data.put("url", resource.getUrl());
        data.put("size", resource.getSize());
        data.put("description", resource.getDescription());
        data.put("uploaderId", resource.getUploaderId());
        data.put("uploadTime", resource.getUploadTime());
        data.put("viewCount", resource.getViewCount());
        data.put("duration", resource.getDuration());
        return data;
    }
}