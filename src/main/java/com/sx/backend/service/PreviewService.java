package com.sx.backend.service;

import com.sx.backend.entity.Resource;
import com.sx.backend.entity.ResourceType;
import com.sx.backend.util.WindowsPathHandler;
import net.coobird.thumbnailator.Thumbnails;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PreviewService {

    private static final Logger log = LoggerFactory.getLogger(PreviewService.class);

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${thumbnail.width}")
    private int thumbnailWidth;

    @Value("${thumbnail.height}")
    private int thumbnailHeight;

    @Value("${thumbnail.quality}")
    private double thumbnailQuality;

    @Autowired
    private DocumentConverter documentConverter;

    @Autowired
    private WindowsPathHandler pathHandler;

    public String generatePreview(Resource resource) throws IOException, OfficeException {
        log.info("开始生成预览，资源ID: {}, 原始类型: {}, URL: {}", 
                resource.getResourceId(), resource.getType(), resource.getUrl());
        
        // 智能确定资源类型：既支持原有ResourceType，又支持根据文件扩展名推断
        ResourceType finalType = determineResourceType(resource);
        log.info("最终确定的资源类型: {}", finalType);
        resource.setType(finalType);
        
        Path sourcePath = pathHandler.getWindowsPath(storageLocation, resource.getUrl());
        log.info("源文件路径: {}", sourcePath);

        switch (resource.getType()) {
            case PDF:
                log.info("处理PDF文件");
                return "/uploads/" + pathHandler.toWebPath(resource.getUrl()); // PDF加上前缀后返回

            case IMAGE:
                log.info("处理图片文件");
                return generateThumbnail(sourcePath, resource.getResourceId());

            case DOCUMENT: // doc, docx
            case PPT: // ppt, pptx
                log.info("处理文档文件，类型: {}", resource.getType());
                return convertToPdf(sourcePath, resource.getResourceId());

            default:
                log.error("不支持的文件类型: {}", resource.getType());
                throw new UnsupportedOperationException("预览不支持此文件类型: " + resource.getType());
        }
    }

    public String generatePreview(Resource resource, String mimeType) throws IOException, OfficeException {
        log.info("开始生成预览，资源ID: {}, 原始类型: {}, URL: {}, MIME: {}", 
                resource.getResourceId(), resource.getType(), resource.getUrl(), mimeType);
        
        // 智能确定资源类型：既支持原有ResourceType，又支持根据文件扩展名和MIME类型推断
        ResourceType finalType = determineResourceType(resource, mimeType);
        log.info("最终确定的资源类型: {}", finalType);
        resource.setType(finalType);
        
        Path sourcePath = pathHandler.getWindowsPath(storageLocation, resource.getUrl());
        log.info("源文件路径: {}", sourcePath);

        switch (resource.getType()) {
            case PDF:

                log.info("处理PDF文件");
                return "/uploads/" + pathHandler.toWebPath(resource.getUrl()); // PDF加上前缀后返回
            case DOCUMENT:
            case PPT:
                return convertToPdf(sourcePath, resource.getResourceId());
            case IMAGE:
                log.info("处理图片文件");
                return generateThumbnail(sourcePath, resource.getResourceId());
            default:
                log.error("不支持的文件类型: {}", resource.getType());
                throw new UnsupportedOperationException("预览不支持此文件类型: " + resource.getType());
        }
    }

    /**
     * 智能确定资源类型：优先使用数据库中的ResourceType，
     * 如果类型为null或者与文件扩展名不匹配，则根据文件扩展名推断
     */
    private ResourceType determineResourceType(Resource resource) {
        ResourceType originalType = resource.getType();
        String url = resource.getUrl();
        
        log.info("原始资源类型: {}, URL: {}", originalType, url);
        
        // 如果URL为空，使用原始类型
        if (url == null || url.isEmpty()) {
            log.info("URL为空，使用原始类型: {}", originalType);
            return originalType != null ? originalType : ResourceType.DOCUMENT;
        }
        
        // 根据文件扩展名推断类型
        String extension = getFileExtension(url);
        log.info("提取的文件扩展名: {}", extension);
        ResourceType inferredType = inferResourceTypeFromExtension(extension);
        log.info("根据扩展名推断的类型: {}", inferredType);
        
        // 如果无法推断类型，使用原始类型
        if (inferredType == null) {
            log.info("无法推断类型，使用原始类型: {}", originalType);
            return originalType != null ? originalType : ResourceType.DOCUMENT;
        }
        
        // 如果原始类型为空，使用推断类型
        if (originalType == null) {
            log.info("原始类型为空，使用推断类型: {}", inferredType);
            return inferredType;
        }
        
        // 检查原始类型是否与文件扩展名匹配
        boolean isMatching = isTypeMatchingExtension(originalType, extension);
        log.info("原始类型与扩展名是否匹配: {}", isMatching);
        
        if (isMatching) {
            log.info("类型匹配，使用原始类型: {}", originalType);
            return originalType; // 使用原始类型
        } else {
            log.info("类型不匹配，使用推断类型: {}", inferredType);
            return inferredType; // 使用推断类型
        }
    }

    /**
     * 根据MIME类型推断ResourceType
     */
    private ResourceType mimeTypeToResourceType(String mimeType) {
        if (mimeType == null) return null;
        switch (mimeType) {
            case "application/pdf":
                return ResourceType.PDF;
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "text/plain":
            case "text/markdown":
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ResourceType.DOCUMENT;
            case "application/vnd.ms-powerpoint":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ResourceType.PPT;
            case "video/mp4":
            case "video/quicktime":
            case "video/x-msvideo":
            case "video/x-matroska":
                return ResourceType.VIDEO;
            case "image/jpeg":
            case "image/png":
            case "image/bmp":
            case "image/webp":
                return ResourceType.IMAGE;
            case "audio/mpeg":
            case "audio/wav":
            case "audio/flac":
            case "audio/aac":
            case "audio/ogg":
            case "audio/x-ms-wma":
            case "audio/mp4":
                return ResourceType.AUDIO;
            case "text/html":
                return ResourceType.LINK;
            default:
                return null;
        }
    }

    /**
     * 智能确定资源类型：优先根据MIME类型判断，
     * 然后根据数据库中的ResourceType和文件扩展名推断
     */
    private ResourceType determineResourceType(Resource resource, String mimeType) {
        ResourceType originalType = resource.getType();
        String url = resource.getUrl();
        
        log.info("原始资源类型: {}, URL: {}, MIME: {}", originalType, url, mimeType);
        
        // 优先根据MIME类型推断
        ResourceType mimeTypeType = mimeTypeToResourceType(mimeType);
        if (mimeTypeType != null) {
            log.info("根据MIME类型推断的类型: {}", mimeTypeType);
            return mimeTypeType;
        }
        
        log.warn("MIME类型 '{}' 无法识别，使用原有逻辑", mimeType);
        // 如果MIME类型无法推断，则使用原有逻辑
        return determineResourceType(resource);
    }

    /**
     * 检查ResourceType是否与文件扩展名匹配
     */
    private boolean isTypeMatchingExtension(ResourceType type, String extension) {
        if (type == null || extension == null || extension.isEmpty()) {
            return false;
        }
        
        String ext = extension.toLowerCase();
        switch (type) {
            case DOCUMENT:
                return ext.equals("doc") || ext.equals("docx") || ext.equals("txt") || 
                       ext.equals("md") || ext.equals("xls") || ext.equals("xlsx");
            case PPT:
                return ext.equals("ppt") || ext.equals("pptx");
            case PDF:
                return ext.equals("pdf");
            case VIDEO:
                return ext.equals("mp4") || ext.equals("mov") || ext.equals("avi") || ext.equals("mkv");
            case IMAGE:
                return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
                       ext.equals("bmp") || ext.equals("webp");
            case AUDIO:
                return ext.equals("mp3") || ext.equals("wav") || ext.equals("flac") || 
                       ext.equals("aac") || ext.equals("ogg") || ext.equals("wma") || ext.equals("m4a");
            case LINK:
                return ext.equals("url") || ext.equals("lnk") || ext.equals("html");
            default:
                return false;
        }
    }

    private String getFileExtension(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < url.length() - 1) {
            return url.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 根据文件扩展名推断ResourceType
     */
    private ResourceType inferResourceTypeFromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        
        switch (extension.toLowerCase()) {
            case "ppt":
            case "pptx":
                return ResourceType.PPT;
            case "pdf":
                return ResourceType.PDF;
            case "mp4":
            case "mov":
            case "avi":
            case "mkv":
                return ResourceType.VIDEO;
            case "doc":
            case "docx":
            case "txt":
            case "md":
            case "xls":
            case "xlsx":
                return ResourceType.DOCUMENT;
            case "url":
            case "lnk":
            case "html":
                return ResourceType.LINK;
            case "jpg":
            case "png":
            case "jpeg":
            case "bmp":
            case "webp":
                return ResourceType.IMAGE;
            case "mp3":
            case "wav":
            case "flac":
            case "aac":
            case "ogg":
            case "wma":
            case "m4a":
                return ResourceType.AUDIO;
            default:
                return null;
        }
    }

    private String generateThumbnail(Path source, String resourceId) throws IOException {
        Path thumbnailDir = pathHandler.getWindowsPath(storageLocation, "thumbnails");
        Files.createDirectories(thumbnailDir);

        Path target = thumbnailDir.resolve(resourceId + ".jpg");
        String escapedSource = pathHandler.escapeSpaces(source.toString());

        Thumbnails.of(new File(escapedSource))
                .size(thumbnailWidth, thumbnailHeight)
                .outputFormat("jpg")
                .outputQuality(thumbnailQuality)
                .toFile(target.toFile());

        return "/thumbnails/" + target.getFileName().toString();
    }

    private String convertToPdf(Path source, String resourceId) throws IOException, OfficeException {
        Path convertedDir = pathHandler.getWindowsPath(storageLocation, "converted");
        Files.createDirectories(convertedDir);

        Path target = convertedDir.resolve(resourceId + ".pdf");
        String escapedSource = pathHandler.escapeSpaces(source.toString());
        String escapedTarget = pathHandler.escapeSpaces(target.toString());

        if (source.toString().toLowerCase().endsWith(".pdf")) {
            // 若原文件已是 PDF，直接复制
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else {
            // 否则进行转换
            documentConverter.convert(new File(escapedSource))
                    .to(new File(escapedTarget))
                    .execute();
        }

        return "/converted/" + target.getFileName().toString();
    }
}
