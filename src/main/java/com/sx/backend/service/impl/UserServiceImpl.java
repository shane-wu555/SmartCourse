package com.sx.backend.service.impl;

import com.sx.backend.dto.request.RegisterRequest;
import com.sx.backend.entity.Role;
import com.sx.backend.entity.Student;
import com.sx.backend.entity.Teacher;
import com.sx.backend.entity.User;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.mapper.TeacherMapper;
import com.sx.backend.mapper.UserMapper;
import com.sx.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper; // 新增
    private final TeacherMapper teacherMapper; // 新增
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserMapper userMapper,
            StudentMapper studentMapper,
            TeacherMapper teacherMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User authenticate(String username, String password) {
        // 1. 根据用户名查找用户
        User user = userMapper.findByUsername(username);

        // 2. 检查用户是否存在
        if (user == null) {
            return null;
        }

        // 3. 验证密码（数据库中的密码是加密存储的）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 4. 返回认证成功的用户对象
        return user;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        // 1. 检查用户名是否已存在
        if (userMapper.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 创建用户对象
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRealName(registerRequest.getRealName()); // 设置真实名
        user.setRole(Role.valueOf(registerRequest.getRole().toUpperCase())); // 确保大写
        user.setRegisterTime(LocalDateTime.now());
        user.setAvatar("default-avatar.png"); // 设置默认头像

        // 3. 插入用户基础信息
        if (userMapper.insertUser(user) != 1) {
            throw new RuntimeException("用户注册失败");
        }

        // 4. 根据角色插入特殊信息
        if ("STUDENT".equals(registerRequest.getRole())) {
            Student student = new Student();
            student.setUserId(user.getUserId());
            student.setStudentNumber(registerRequest.getStudentNumber());
            student.setGrade(registerRequest.getGrade());
            student.setMajor(registerRequest.getMajor());
            studentMapper.insertStudent(student);
        }  else if ("TEACHER".equals(registerRequest.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getUserId());
            teacher.setTitle(registerRequest.getTitle());
            teacher.setDepartment(registerRequest.getDepartment());
            teacher.setBio(registerRequest.getBio());
            teacherMapper.insertTeacher(teacher);
        }

        return user;
    }

    @Override
    public void updateLastLoginTime(String userId) {
        userMapper.updateLastLoginTime(userId, LocalDateTime.now());
    }

}
