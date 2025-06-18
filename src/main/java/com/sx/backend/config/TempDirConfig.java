package com.sx.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class TempDirConfig {

    @Value("${file.temp-dir}")
    private String tempDirPath;

    @PostConstruct
    public void init() {
        try {
            Path tempDir = Paths.get(tempDirPath);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建临时目录: " + tempDirPath, e);
        }
    }
}
