package com.sx.backend.exception;

import com.sx.backend.controller.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        int statusCode;
        switch (ex.getCode()) {
            case 401: statusCode = HttpStatus.UNAUTHORIZED.value(); break;
            case 403: statusCode = HttpStatus.FORBIDDEN.value(); break;
            case 404: statusCode = HttpStatus.NOT_FOUND.value(); break;
            case 409: statusCode = HttpStatus.CONFLICT.value(); break;
            default: statusCode = HttpStatus.BAD_REQUEST.value();
        }
        return ResponseEntity.status(statusCode)
                .body(new ApiResponse<>(ex.getCode(), ex.getMessage(), null));
    }

    // 处理参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, errorMsg, null));
    }

    // 处理参数异常（如IllegalArgumentException）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, ex.getMessage(), null));
    }

    // 处理其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex, HttpServletRequest request) {
        // 检查是否是视频播放或资源访问相关的请求
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/play") || requestURI.contains("/download") || 
            requestURI.contains("/resources/") && (requestURI.endsWith("/play") || requestURI.endsWith("/download"))) {
            // 对于视频流等资源接口，直接返回HTTP状态码，不返回JSON
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        // 对于其他接口，返回JSON格式的错误响应
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "服务器内部错误", null));
    }

}
