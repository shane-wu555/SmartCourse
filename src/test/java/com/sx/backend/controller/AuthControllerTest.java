package com.sx.backend.controller;

import com.sx.backend.dto.request.LoginRequest;
import com.sx.backend.dto.request.RegisterRequest;
import com.sx.backend.entity.User;
import com.sx.backend.entity.Role;
import com.sx.backend.service.TokenBlacklistService;
import com.sx.backend.service.UserService;
import com.sx.backend.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        User user = new User();
        user.setUserId("1");
        user.setUsername("testuser");
        user.setRole(Role.STUDENT);

        when(userService.authenticate("testuser", "testpass")).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn("mocked-jwt");

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("testuser", body.get("username"));
        assertEquals("STUDENT", body.get("role"));
        assertEquals("mocked-jwt", body.get("token"));

        verify(userService).updateLastLoginTime("1");
    }

    @Test
    void testLogin_failed() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpass");

        when(userService.authenticate("testuser", "wrongpass")).thenReturn(null);

        ResponseEntity<?> response = authController.login(loginRequest);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("用户名或密码错误", response.getBody());
    }

    @Test
    void testRegister_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("pass");

        User user = new User();
        user.setUserId("2");
        user.setUsername("newuser");
        user.setRole(Role.ADMIN);

        when(userService.registerUser(request)).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn("new-token");

        ResponseEntity<?> response = authController.register(request);
        assertEquals(201, response.getStatusCodeValue());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("2", body.get("userId"));
        assertEquals("ADMIN", body.get("role"));
        assertEquals("new-token", body.get("token"));
    }

    @Test
    void testRegister_usernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("123");

        when(userService.registerUser(request)).thenThrow(new IllegalArgumentException("用户名已存在"));

        ResponseEntity<?> response = authController.register(request);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("用户名已存在"));
    }

    @Test
    void testRegister_otherException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("boom");
        request.setPassword("123");

        when(userService.registerUser(request)).thenThrow(new RuntimeException("服务器异常"));

        ResponseEntity<?> response = authController.register(request);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("注册失败", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void testLogout_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        ResponseEntity<?> response = authController.logout(request);

        verify(tokenBlacklistService).addToBlacklist("abc.def.ghi");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("登出成功", response.getBody());
    }

    @Test
    void testLogout_missingToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<?> response = authController.logout(request);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("无效的令牌"));
    }
}
