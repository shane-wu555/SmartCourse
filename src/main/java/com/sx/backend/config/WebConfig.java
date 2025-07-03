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
                    "/api/debug/**",          // 调试接口
                    "/api/preview/**",        // 预览接口
                    "/api/video/**",          // 视频播放接口
                    "/api/videos/**",         // 视频播放接口(兼容)
                    "/api/resources/**",      // 资源访问接口
                    "/api/teacher/resources/**", // 教师资源相关接口
                    "/api/student/resources/**", // 学生资源相关接口
                    "/api/teacher/courses/*/resources/**", // 课程资源接口
                    "/uploads/**",            // 上传文件静态资源
                    "/converted/**",          // 转换后的文件
                    "/thumbnails/**",         // 缩略图文件
                    "/videos/**",             // 视频文件
                    "/documents/**",          // 文档文件
                    "/images/**",             // 图片文件
                    "/submissions/**"         // 提交文件
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
        
        // 视频文件
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + fileStorageLocation + "/videos/");
        
        // 文档文件
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:" + fileStorageLocation + "/documents/");
        
        // 图片文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileStorageLocation + "/images/");
        
        // 提交文件
        registry.addResourceHandler("/submissions/**")
                .addResourceLocations("file:" + fileStorageLocation + "/submissions/");
        
        // 课程资源文件访问（兼容性，包括文档、图片、视频等）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileStorageLocation + "/");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://localhost:8081", "http://localhost:3000") // 指定具体域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
