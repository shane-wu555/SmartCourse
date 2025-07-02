package com.sx.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户认证拦截器
 * TODO: 实现完整的JWT或Session认证机制
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // TODO: 实现真正的用户认证逻辑
        // 这里可以解析JWT token、检查session等
        
        // 暂时设置一个默认用户ID，避免控制器中的认证检查失败
        String userId = extractUserIdFromRequest(request);
        if (userId == null || userId.isEmpty()) {
            // 如果没有用户信息，设置一个默认值
            userId = "default-user-" + System.currentTimeMillis();
        }
        
        request.setAttribute("userId", userId);
        log.debug("设置用户ID: {}", userId);
        
        return true;
    }

    /**
     * 从请求中提取用户ID
     * TODO: 实现真正的用户认证逻辑
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        // 方案1: 从JWT Token中解析
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // String token = authHeader.substring(7);
            // return parseUserIdFromJwt(token);
        }
        
        // 方案2: 从Session中获取
        // HttpSession session = request.getSession(false);
        // if (session != null) {
        //     return (String) session.getAttribute("userId");
        // }
        
        // 方案3: 从请求参数中获取（仅用于测试）
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) {
            return userIdParam;
        }
        
        // 暂时返回null，让上面的逻辑设置默认值
        return null;
    }
}
