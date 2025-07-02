package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
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
            HttpServletRequest request, // 新增参数
            @PathVariable String courseId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description) { // 移除 uploaderId 参数

        try {
            // TODO: 实现完整的用户认证机制
            // 暂时使用默认用户ID
            String uploaderId = "default-user-id";
            
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

    // === 视频播放接口 (支持流式传输) ===
    @GetMapping("/resources/{resourceId}/play")
    public ResponseEntity<org.springframework.core.io.Resource> playVideo(
            HttpServletRequest request,
            @PathVariable String resourceId) {

        try {
            // TODO: 实现完整的用户认证机制
            // 暂时跳过认证检查，允许所有请求访问视频资源
            
            Resource dbResource = resourceMapper.getResourceById(resourceId);
            if (dbResource == null) {
                return ResponseEntity.status(404).build();
            }

            // 检查是否为视频资源
            if (dbResource.getType() != ResourceType.VIDEO) {
                return ResponseEntity.badRequest().build();
            }

            // 构建文件路径
            Path storageRoot = Paths.get(storageLocation);
            Path filePath = storageRoot.resolve(dbResource.getUrl().replaceFirst("^/", ""));

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(410).build(); // 文件已不存在
            }

            org.springframework.core.io.Resource fileResource = new UrlResource(filePath.toUri());

            // 设置视频响应头，支持流式播放
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "video/mp4");
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // === 通用资源访问接口 (兼容旧的前端请求) ===
    @GetMapping("/resources/{resourceId}")
    public ResponseEntity<?> accessResource(
            HttpServletRequest request,
            @PathVariable String resourceId) {

        log.info("=== 资源访问请求 ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Resource ID: {}", resourceId);
        log.info("==================");

        try {
            // TODO: 实现完整的用户认证机制
            // 暂时跳过认证检查，允许所有请求访问资源
            
            Resource dbResource = resourceMapper.getResourceById(resourceId);
            if (dbResource == null) {
                return ResponseEntity.status(404)
                        .body(errorResponse(404, "资源不存在"));
            }

            // 调试：打印资源信息
            System.out.println("=== 资源调试信息 ===");
            System.out.println("Resource ID: " + dbResource.getResourceId());
            System.out.println("Resource Type: " + dbResource.getType());
            System.out.println("Resource URL: " + dbResource.getUrl());
            System.out.println("Resource Name: " + dbResource.getName());
            System.out.println("==================");

            // 如果是视频资源，直接返回文件流用于播放
            if (dbResource.getType() == ResourceType.VIDEO) {
                return handleVideoStream(request, dbResource);
            } else {
                // 非视频资源返回详情信息
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "成功获取资源详情");
                response.put("data", createResourceResponse(dbResource));
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "访问资源失败: " + e.getMessage()));
        }
    }

    // 处理视频流请求
    private ResponseEntity<?> handleVideoStream(HttpServletRequest request, Resource dbResource) {
        try {
            Path storageRoot = Paths.get(storageLocation);
            Path filePath = storageRoot.resolve(dbResource.getUrl().replaceFirst("^/", ""));

            // 调试信息
            log.info("=== 视频流处理 ===");
            log.info("文件路径: {}", filePath);
            log.info("文件存在: {}", Files.exists(filePath));
            log.info("================");

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(410)
                        .body(errorResponse(410, "视频文件不存在"));
            }

            // 使用FileSystemResource代替UrlResource
            FileSystemResource fileResource = new FileSystemResource(filePath);
            
            if (!fileResource.exists()) {
                return ResponseEntity.status(410)
                        .body(errorResponse(410, "视频文件不可读"));
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String contentType = getVideoContentType(filePath.toString());
            
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.set(HttpHeaders.PRAGMA, "no-cache");
            headers.set(HttpHeaders.EXPIRES, "0");
            
            long contentLength = fileResource.contentLength();
            headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));

            log.info("返回视频: {}, 类型: {}, 大小: {} bytes", filePath.getFileName(), contentType, contentLength);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
                    
        } catch (Exception e) {
            log.error("处理视频流时出错: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(errorResponse(500, "视频流处理失败: " + e.getMessage()));
        }
    }

    // 获取视频内容类型
    private String getVideoContentType(String filename) {
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
        
        // 调试MIME类型获取
        String mimeType = getMimeTypeFromUrl(resource.getUrl());
        log.info("=== MIME类型调试 ===");
        log.info("资源URL: {}", resource.getUrl());
        log.info("计算出的MIME类型: {}", mimeType);
        log.info("================");
        
        data.put("type", mimeType);
        data.put("url", resource.getUrl());
        data.put("size", resource.getSize());
        data.put("description", resource.getDescription());
        data.put("uploaderId", resource.getUploaderId());
        data.put("uploadTime", resource.getUploadTime());
        data.put("viewCount", resource.getViewCount());
        data.put("duration", resource.getDuration());
        return data;
    }

    // 根据文件URL获取MIME类型
    private String getMimeTypeFromUrl(String url) {
        log.info("=== MIME类型解析调试 ===");
        log.info("输入URL: {}", url);
        
        if (url == null || url.isEmpty()) {
            log.info("URL为空，返回默认类型");
            return "application/octet-stream";
        }
        
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        log.info("提取的文件名: {}", fileName);
        
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            log.info("文件名中没有扩展名，返回默认类型");
            return "application/octet-stream";
        }
        
        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        log.info("提取的扩展名: {}", ext);
        
        String mimeType;
        switch (ext) {
            // 视频文件类型
            case "mp4": mimeType = "video/mp4"; break;
            case "avi": mimeType = "video/x-msvideo"; break;
            case "mov": mimeType = "video/quicktime"; break;
            case "wmv": mimeType = "video/x-ms-wmv"; break;
            case "flv": mimeType = "video/x-flv"; break;
            case "webm": mimeType = "video/webm"; break;
            case "mkv": mimeType = "video/x-matroska"; break;
            
            // 音频文件类型
            case "mp3": mimeType = "audio/mpeg"; break;
            case "wav": mimeType = "audio/wav"; break;
            case "flac": mimeType = "audio/flac"; break;
            case "aac": mimeType = "audio/aac"; break;
            
            // 图片文件类型
            case "jpg": case "jpeg": mimeType = "image/jpeg"; break;
            case "png": mimeType = "image/png"; break;
            case "gif": mimeType = "image/gif"; break;
            case "bmp": mimeType = "image/bmp"; break;
            case "webp": mimeType = "image/webp"; break;
            
            // 文档文件类型
            case "pdf": mimeType = "application/pdf"; break;
            case "doc": mimeType = "application/msword"; break;
            case "docx": mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; break;
            case "ppt": mimeType = "application/vnd.ms-powerpoint"; break;
            case "pptx": mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"; break;
            case "xls": mimeType = "application/vnd.ms-excel"; break;
            case "xlsx": mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; break;
            case "txt": mimeType = "text/plain"; break;
            
            default: mimeType = "application/octet-stream"; break;
        }
        
        log.info("最终MIME类型: {}", mimeType);
        log.info("=====================");
        return mimeType;
    }

    // === 测试接口 ===
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ResourceController is working!");
    }
}