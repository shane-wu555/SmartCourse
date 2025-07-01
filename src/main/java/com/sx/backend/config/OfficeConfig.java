package com.sx.backend.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OfficeConfig {

    // LibreOffice 安装路径，例如：C:/Program Files/LibreOffice
    @Value("${libreoffice.home}")
    private String officeHome;

    // LibreOffice 监听端口（可配置多个端口，用逗号隔开，例如：2002,2003）
    @Value("${libreoffice.ports}")
    private String ports;

    /**
     * 配置并启动 OfficeManager（LibreOffice 后台服务）
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager officeManager() {
        int[] portNumbers = Arrays.stream(ports.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();

        return LocalOfficeManager.builder()
                .officeHome(officeHome)                // 设置 LibreOffice 路径
                .portNumbers(portNumbers)              // 设置端口（支持多个）
                .maxTasksPerProcess(20)                // 每个进程最多任务数
                .taskExecutionTimeout(300000L)         // 每个任务最大执行时间（5分钟）
                .build();
    }

    /**
     * 注入 DocumentConverter，供服务中转换文档使用
     */
    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }
}
