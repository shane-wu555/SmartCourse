package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
@RequestMapping("/api/teacher")
public class ResourceController {

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${python.executable:python}")
    private String pythonExecutable;

    private final ResourceMapper resourceMapper;
    private Path pythonScriptPath;

    public ResourceController(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
        this.pythonScriptPath = extractPythonScript();
    }

    // === 资源上传接口 ===
    @PostMapping("/courses/{courseId}/resources")
    public ResponseEntity<Map<String, Object>> uploadResource(
            @PathVariable String courseId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("uploaderId") String uploaderId) {

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse(400, "文件不能为空"));
            }

            ResourceType resourceType = validateFileType(file, type);
            validateFileSize(file);

            String originalFilename = file.getOriginalFilename();
            String safeFilename = generateSafeFilename(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "_" + safeFilename;

            Path courseDir = Paths.get(storageLocation, courseId, resourceType.toString().toLowerCase());
            if (!Files.exists(courseDir)) {
                Files.createDirectories(courseDir);
            }

            Path filePath = courseDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            Resource resource = new Resource();
            resource.setResourceId(UUID.randomUUID().toString());
            resource.setCourseId(courseId);
            resource.setName(name);
            resource.setType(resourceType);
            // 存储相对路径而不是绝对路径
            String relativePath = "/" + courseId + "/" + resourceType.toString().toLowerCase() + "/" + uniqueFilename;
            resource.setUrl(relativePath);
            resource.setSize(file.getSize());
            resource.setDescription(description);
            resource.setUploaderId(uploaderId);
            resource.setUploadTime(LocalDateTime.now());
            resource.setViewCount(0);

            if (resourceType == ResourceType.VIDEO) {
                try {
                    String winPath = filePath.toString().replace("\\", "\\\\");
                    Float duration = parseVideoDuration(Paths.get(winPath));
                    resource.setDuration(duration);
                } catch (Exception e) {
                    resource.setDuration(null);
                }
            } else {
                resource.setDuration(null);
            }

            resourceMapper.insertResource(resource);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "资源上传成功");
            response.put("data", createResourceResponse(resource));

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorResponse(400, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(errorResponse(500, "文件保存失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(500, "服务器错误: " + e.getMessage()));
        }
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

    private Float parseVideoDuration(Path videoFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExecutable,
                pythonScriptPath.toString(),
                videoFile.toString()
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String output = reader.readLine();
                if (output != null && !output.trim().isEmpty()) {
                    return Float.parseFloat(output.trim());
                }
            }
        } else {
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }
            throw new IOException("Python脚本错误 (exit code " + exitCode + "): " + error);
        }

        throw new IOException("未获取到有效时长数据");
    }

    private Map<String, Object> errorResponse(int code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("error", message);
        return error;
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过100MB限制");
        }
    }

    private String generateSafeFilename(String filename) {
        String safeName = filename.replaceAll(".*[/\\\\]", "");
        safeName = safeName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        safeName = safeName.replaceAll("\\.\\.", "_");
        return safeName;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private ResourceType validateFileType(MultipartFile file, String type) {
        ResourceType resourceType;
        try {
            resourceType = ResourceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的文件类型: " + type);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("文件名无效");
        }

        String extension = getFileExtension(filename).toLowerCase();
        Set<String> allowedExtensions = getAllowedExtensions(resourceType);

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件扩展名: " + extension);
        }

        return resourceType;
    }

    private Set<String> getAllowedExtensions(ResourceType type) {
        switch (type) {
            case PPT: return Set.of("ppt", "pptx");
            case PDF: return Set.of("pdf");
            case VIDEO: return Set.of("mp4", "mov", "avi", "mkv");
            case DOCUMENT: return Set.of("doc", "docx", "txt", "md", "xls", "xlsx");
            case LINK: return Set.of("url", "lnk", "html");
            case IMAGE: return Set.of("jpg", "png", "jpeg", "bmp", "webp");
            case AUDIO: return Set.of("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a");
            default: return Collections.emptySet();
        }
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