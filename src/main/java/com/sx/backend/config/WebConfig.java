package com.sx.backend.config;

import com.sx.backend.security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",           // 认证相关接口
                    "/api/teacher/resources/**", // 资源相关接口
                    "/api/teacher/courses/*/resources/**" // 课程资源接口
                );
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/converted/**")
                .addResourceLocations("file:C:/Users/86150/Desktop/SmartCourse/uploads/converted/");

        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:C:/Users/86150/Desktop/SmartCourse/uploads/thumbnails/");
    }
}
