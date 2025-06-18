package com.sx.backend.security;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 放行登录和注册接口
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/api/auth/login") ||
                requestURI.contains("/api/auth/register")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证信息");
            return false;
        }

        String token = authHeader.substring(7); // 去掉"Bearer "前缀
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
