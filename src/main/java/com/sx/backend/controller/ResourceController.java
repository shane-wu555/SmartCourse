package com.sx.backend.controller;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/teacher/courses/{courseId}/resources")
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

    // 从classpath提取Python脚本到临时文件
    private Path extractPythonScript() {
        try {
            // 使用全限定名避免冲突
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

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadResource(
            @PathVariable String courseId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("uploaderId") String uploaderId) {

        // 添加调试日志
        System.out.println("Received upload request for course: " + courseId);
        System.out.println("File present: " + (file != null));
        System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Description: " + description);
        System.out.println("Uploader ID: " + uploaderId);

        try {
            // 1. 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse(400, "文件不能为空"));
            }

            // 2. 验证文件类型与大小
            ResourceType resourceType = validateFileType(file, type);
            validateFileSize(file);

            // 3. 生成安全文件名和存储路径
            String originalFilename = file.getOriginalFilename();
            String safeFilename = generateSafeFilename(originalFilename);
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "_" + safeFilename;

            // 4. 创建存储目录
            Path courseDir = Paths.get(storageLocation, courseId, resourceType.toString().toLowerCase());
            if (!Files.exists(courseDir)) {
                Files.createDirectories(courseDir);
            }

            // 5. 保存文件
            Path filePath = courseDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // 6. 创建资源实体
            Resource resource = new Resource();
            resource.setResourceId(UUID.randomUUID().toString());
            resource.setCourseId(courseId);
            resource.setName(name);
            resource.setType(resourceType);
            resource.setUrl(filePath.toString());
            resource.setSize(file.getSize());
            resource.setDescription(description);
            resource.setUploaderId(uploaderId);
            resource.setUploadTime(LocalDateTime.now());
            resource.setViewCount(0);

            // ======== 新增：视频时长解析 ========
            if (resourceType == ResourceType.VIDEO) {
                try {
                    // Windows 路径处理
                    String winPath = filePath.toString().replace("\\", "\\\\");
                    Float duration = parseVideoDuration(Paths.get(winPath));
                    resource.setDuration(duration);
                    System.out.println("视频时长解析成功: " + duration + "秒");
                } catch (Exception e) {
                    System.err.println("视频时长解析失败: " + e.getMessage());
                    e.printStackTrace();
                    resource.setDuration(null);
                }
            } else {
                resource.setDuration(null); // 非视频资源设为null
            }

            // 7. 保存到数据库
            resourceMapper.insertResource(resource);

            // 8. 返回响应
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

    // 调用Python脚本解析视频时长
    private Float parseVideoDuration(Path videoFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExecutable,
                pythonScriptPath.toString(),
                videoFile.toString()
        );

        // 重定向错误流以便调试
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
            // 读取错误输出
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

    // === 以下是缺失的方法实现 ===

    // 错误响应方法
    private Map<String, Object> errorResponse(int code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("error", message);
        return error;
    }

    // 验证文件大小
    private void validateFileSize(MultipartFile file) {
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过100MB限制");
        }
    }

    // 生成安全文件名
    private String generateSafeFilename(String filename) {
        // 移除路径信息
        String safeName = filename.replaceAll(".*[/\\\\]", "");
        // 替换特殊字符
        safeName = safeName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        // 防止路径遍历
        safeName = safeName.replaceAll("\\.\\.", "_");
        return safeName;
    }

    // 获取文件扩展名
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    // 验证文件类型
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

    // 获取允许的文件扩展名
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

    // 创建资源响应
    private Map<String, Object> createResourceResponse(Resource resource) {
        Map<String, Object> data = new HashMap<>();
        data.put("resourceId", resource.getResourceId());
        data.put("name", resource.getName());
        data.put("type", resource.getType().name());
        data.put("url", resource.getUrl());
        data.put("size", resource.getSize());
        data.put("description", resource.getDescription());
        data.put("uploaderId", resource.getUploaderId());
        data.put("uploadTime", resource.getUploadTime());
        data.put("viewCount", resource.getViewCount());
        data.put("duration", resource.getDuration()); // 新增时长字段
        return data;
    }
}