package com.sx.backend.security;

import com.sx.backend.service.TokenBlacklistService;
import com.sx.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 放行预检请求（OPTIONS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行登录和注册接口以及静态资源
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/api/auth/login") ||
                requestURI.contains("/api/auth/register") ||
                requestURI.startsWith("/uploads/") ||
                requestURI.startsWith("/converted/") ||
                requestURI.startsWith("/thumbnails/") ||
                requestURI.startsWith("/videos/") ||
                requestURI.startsWith("/documents/") ||
                requestURI.startsWith("/images/") ||
                requestURI.startsWith("/submissions/") ||
                requestURI.contains("/api/preview/") ||
                requestURI.contains("/api/video/") ||
                requestURI.contains("/api/videos/") ||
                requestURI.contains("/api/resources/") ||
                requestURI.contains("/api/teacher/resources/") ||
                requestURI.contains("/api/student/resources/")) {
            return true;
        }

        // 验证Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证信息");
            return false;
        }

        // 提取Token并检查黑名单
        String token = authHeader.substring(7);
        if (tokenBlacklistService.isBlacklisted(token)) {  // 新增黑名单检查
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "认证信息已失效");
            return false;
        }

        // 验证Token有效性
        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "认证信息无效或已过期");
            return false;
        }

        // 将用户信息存入请求属性中
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("userRole", jwtUtil.getRoleFromToken(token));
        return true;
    }
}
