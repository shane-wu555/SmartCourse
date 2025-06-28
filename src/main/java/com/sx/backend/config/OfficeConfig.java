package com.sx.backend.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class OfficeConfig {

    @Value("${libreoffice.home}")
    private String officeHome;

    @Value("${libreoffice.ports}")
    private String ports;

    @Bean
    public OfficeManager officeManager() {
        int[] portNumbers = Arrays.stream(ports.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();

        return LocalOfficeManager.builder()
                .officeHome(officeHome)
                .portNumbers(portNumbers)
                .maxTasksPerProcess(20)
                .taskExecutionTimeout(300000L) // 5分钟超时
                .build();
    }

    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }
}