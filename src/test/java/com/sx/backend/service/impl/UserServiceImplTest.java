package com.sx.backend.service.impl;

import com.sx.backend.dto.request.RegisterRequest;
import com.sx.backend.entity.Role;
import com.sx.backend.entity.Student;
import com.sx.backend.entity.Teacher;
import com.sx.backend.entity.User;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.mapper.TeacherMapper;
import com.sx.backend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_shouldReturnUser_whenPasswordMatches() {
        String username = "testuser";
        String rawPassword = "rawPass";
        String encodedPassword = "encodedPass";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword(encodedPassword);

        when(userMapper.findByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        User result = userService.authenticate(username, rawPassword);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userMapper).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void authenticate_shouldReturnNull_whenUserNotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        User result = userService.authenticate("nonexistent", "anyPass");

        assertNull(result);
        verify(userMapper).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_shouldReturnNull_whenPasswordMismatch() {
        String username = "user";
        User mockUser = new User();
        mockUser.setPassword("encodedPass");
        when(userMapper.findByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        User result = userService.authenticate(username, "wrongPass");

        assertNull(result);
        verify(userMapper).findByUsername(username);
        verify(passwordEncoder).matches("wrongPass", "encodedPass");
    }

    @Test
    void registerUser_shouldThrow_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");

        when(userMapper.existsByUsername("existingUser")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("用户名已存在", ex.getMessage());

        verify(userMapper).existsByUsername("existingUser");
        verify(userMapper, never()).insertUser(any());
    }

    @Test
    void registerUser_shouldInsertStudent_whenRoleStudent() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("studentUser");
        request.setPassword("pass");
        request.setEmail("email@example.com");
        request.setPhone("123456789");
        request.setRealName("Student RealName");
        request.setRole("STUDENT");
        request.setStudentNumber("S123");
        request.setGrade("Grade1");
        request.setMajor("MajorX");

        when(userMapper.existsByUsername("studentUser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userMapper.insertUser(any())).thenReturn(1);
        when(studentMapper.insertStudent(any())).thenReturn(1);

        User user = userService.registerUser(request);

        assertNotNull(user);
        assertEquals("studentUser", user.getUsername());
        assertEquals(Role.STUDENT, user.getRole());

        verify(userMapper).existsByUsername("studentUser");
        verify(passwordEncoder).encode("pass");
        verify(userMapper).insertUser(any());
        verify(studentMapper).insertStudent(any());
        verify(teacherMapper, never()).insertTeacher(any());
    }

    @Test
    void registerUser_shouldInsertTeacher_whenRoleTeacher() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("teacherUser");
        request.setPassword("pass");
        request.setEmail("teacher@example.com");
        request.setPhone("987654321");
        request.setRealName("Teacher RealName");
        request.setRole("TEACHER");
        request.setEmployeeNumber("E456");
        request.setTitle("Professor");
        request.setDepartment("CS");
        request.setBio("Bio info");

        when(userMapper.existsByUsername("teacherUser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userMapper.insertUser(any())).thenReturn(1);
        when(teacherMapper.insertTeacher(any())).thenReturn(1);

        User user = userService.registerUser(request);

        assertNotNull(user);
        assertEquals("teacherUser", user.getUsername());
        assertEquals(Role.TEACHER, user.getRole());

        verify(userMapper).existsByUsername("teacherUser");
        verify(passwordEncoder).encode("pass");
        verify(userMapper).insertUser(any());
        verify(teacherMapper).insertTeacher(any());
        verify(studentMapper, never()).insertStudent(any());
    }

    @Test
    void registerUser_shouldThrow_whenInsertUserFails() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("pass");
        request.setRole("STUDENT");

        when(userMapper.existsByUsername("newUser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userMapper.insertUser(any())).thenReturn(0); // 插入失败

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.registerUser(request));
        assertEquals("用户注册失败", ex.getMessage());

        verify(userMapper).insertUser(any());
        verify(studentMapper, never()).insertStudent(any());
        verify(teacherMapper, never()).insertTeacher(any());
    }

    @Test
    void updateLastLoginTime_shouldCallMapper() {
        String userId = "user123";

        when(userMapper.updateLastLoginTime(eq(userId), any(LocalDateTime.class))).thenReturn(1);

        userService.updateLastLoginTime(userId);

        verify(userMapper).updateLastLoginTime(eq(userId), any(LocalDateTime.class));
    }

}
