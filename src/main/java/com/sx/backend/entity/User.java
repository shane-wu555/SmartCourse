package com.sx.backend.entity;

import java.time.LocalDateTime;

public class User {
    private String userId;
    private String username;
    private String password; // 加密存储
    private String email;
    private String phone;
    private String avatar; // 头像URL
    private String realName;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Role role; // 枚举类型：教师/学生

    // 构造方法
    public User(String userId, String username, String password, String email, String phone, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.registerTime = LocalDateTime.now();
    }

    public User(String userId, String username, String password, String email, String phone, String realName, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.realName = realName;
        this.role = role;
        this.registerTime = LocalDateTime.now();
    }

    public User() {
    }

    public String getRealName() { return realName; }

    public void setRealName(String realName) { this.realName = realName; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() { return userId;  }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}


