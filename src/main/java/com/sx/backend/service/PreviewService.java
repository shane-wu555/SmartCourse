package com.sx.backend.service;

import com.sx.backend.entity.Resource;
import com.sx.backend.util.WindowsPathHandler;
import net.coobird.thumbnailator.Thumbnails;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PreviewService {

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
        Path sourcePath = pathHandler.getWindowsPath(storageLocation, resource.getUrl());

        switch (resource.getType()) {
            case PDF:
                return resource.getUrl(); // PDF直接返回原路径

            case IMAGE:
                return generateThumbnail(sourcePath, resource.getResourceId());

            case DOCUMENT: // doc, docx
            case PPT: // ppt, pptx
                return convertToPdf(sourcePath, resource.getResourceId());

            default:
                throw new UnsupportedOperationException("预览不支持此文件类型: " + resource.getType());
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

        return pathHandler.toWebPath("thumbnails/" + target.getFileName().toString());
    }

    private String convertToPdf(Path source, String resourceId) throws OfficeException, IOException {
        Path convertedDir = pathHandler.getWindowsPath(storageLocation, "converted");
        Files.createDirectories(convertedDir);

        Path target = convertedDir.resolve(resourceId + ".pdf");
        String escapedSource = pathHandler.escapeSpaces(source.toString());
        String escapedTarget = pathHandler.escapeSpaces(target.toString());

        documentConverter.convert(new File(escapedSource))
                .to(new File(escapedTarget))
                .execute();

        return pathHandler.toWebPath("converted/" + target.getFileName().toString());
    }
}