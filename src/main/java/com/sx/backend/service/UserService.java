package com.sx.backend.service;

import com.sx.backend.dto.request.RegisterRequest;
import com.sx.backend.entity.User;

public interface UserService {
    User authenticate(String username, String password);
    User registerUser(RegisterRequest registerRequest);
    void updateLastLoginTime(String userId);
}
