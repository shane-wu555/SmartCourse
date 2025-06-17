package com.sx.backend.exception;

import com.sx.backend.controller.ApiResponse;
import com.sx.backend.service.impl.BusinessException;
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

    // 处理其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "服务器内部错误", null));
    }
}
