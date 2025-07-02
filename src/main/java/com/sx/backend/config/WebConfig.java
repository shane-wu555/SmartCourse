package com.sx.backend.config;

import com.sx.backend.security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",           // 认证相关接口
                    "/api/preview/**",        // 预览相关接口
                    "/api/resources/**",      // 资源访问接口
                    "/api/teacher/resources/**", // 资源相关接口
                    "/api/teacher/courses/*/resources/**", // 课程资源接口
                    "/uploads/**",            // 上传文件静态资源
                    "/converted/**",          // 转换后的文件
                    "/thumbnails/**"          // 缩略图文件
                );
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 转换后的文件（PDF等）
        registry.addResourceHandler("/converted/**")
                .addResourceLocations("file:" + fileStorageLocation + "/converted/");

        // 缩略图
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + fileStorageLocation + "/thumbnails/");
        
        // 课程资源文件访问（包括文档、图片、视频等）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileStorageLocation + "/");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
