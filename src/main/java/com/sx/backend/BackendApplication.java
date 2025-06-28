package com.sx.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // 启用 headless 模式
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(BackendApplication.class, args);
    }
}