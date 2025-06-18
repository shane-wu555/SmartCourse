package com.sx.backend.controller;


import com.sx.backend.dto.request.LoginRequest;
import com.sx.backend.dto.request.RegisterRequest;
import com.sx.backend.entity.User;
import com.sx.backend.service.TokenBlacklistService;
import com.sx.backend.service.UserService;
import com.sx.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil
    , TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. 验证用户凭证
        User user = userService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }

        userService.updateLastLoginTime(user.getUserId()); // 更新登录时间

        // 3. 生成 JWT
        String token = jwtUtil.generateToken(user);

        // 4. 构造响应
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 1. 注册用户
            User user = userService.registerUser(registerRequest);

            // 2. 生成JWT
            String token = jwtUtil.generateToken(user);

            // 3. 构造响应
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
            response.put("token", token);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // 处理用户名已存在等业务异常
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 处理其他异常
            e.printStackTrace(); // 打印详细异常信息
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "注册失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // AuthController.java 新增方法
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.addToBlacklist(token);
            return ResponseEntity.ok("登出成功");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "无效的令牌"));
    }
}