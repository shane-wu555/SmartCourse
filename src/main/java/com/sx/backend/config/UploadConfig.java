package com.sx.backend.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;


@Configuration
public class UploadConfig {

    @Value("${file.temp-dir}")
    private String tempDirPath;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(tempDirPath);
    }
}
