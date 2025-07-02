package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminStudentResponse;
import com.sx.backend.entity.Student;
import com.sx.backend.entity.User;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.mapper.UserMapper;
import com.sx.backend.util.ExcelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AdminStudentServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private AdminStudentServiceImpl adminStudentService;

    // 测试数据
    private final String STUDENT_NUMBER = "20240001";
    private final String REAL_NAME = "张三";
    private final String GRADE = "2024";
    private final String MAJOR = "计算机科学";
    private final String USER_ID = UUID.randomUUID().toString();
    private final String EMAIL = STUDENT_NUMBER + "@neu.edu.cn";

    @Test
    void createStudent_Success() {
        // 准备请求
        AdminStudentCreateRequest request = new AdminStudentCreateRequest();
        request.setStudentNumber(STUDENT_NUMBER);
        request.setRealName(REAL_NAME);
        request.setGrade(GRADE);
        request.setMajor(MAJOR);

        // 模拟依赖行为
        when(studentMapper.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(false);
        when(userMapper.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(STUDENT_NUMBER)).thenReturn("encodedPassword");
        when(userMapper.insertUser(any(User.class))).thenReturn(1);
        when(studentMapper.insertStudent(any(Student.class))).thenReturn(1);

        // 执行方法
        AdminStudentResponse response = adminStudentService.createStudent(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(REAL_NAME, response.getRealName());
        assertEquals(STUDENT_NUMBER, response.getStudentNumber());
        verify(userMapper, times(1)).insertUser(any(User.class));
        verify(studentMapper, times(1)).insertStudent(any(Student.class));
    }

    @Test
    void createStudent_DuplicateStudentNumber() {
        AdminStudentCreateRequest request = new AdminStudentCreateRequest();
        request.setStudentNumber(STUDENT_NUMBER);

        when(studentMapper.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminStudentService.createStudent(request)
        );
        assertEquals(409, exception.getCode());
        assertEquals("学号已存在", exception.getMessage());
    }

    @Test
    void createStudent_UserInsertFailed() {
        AdminStudentCreateRequest request = new AdminStudentCreateRequest();
        request.setStudentNumber(STUDENT_NUMBER);
        request.setRealName(REAL_NAME);

        when(studentMapper.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(false);
        when(userMapper.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.insertUser(any(User.class))).thenReturn(0); // 插入失败

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminStudentService.createStudent(request)
        );
        assertEquals(500, exception.getCode());
        assertEquals("用户创建失败", exception.getMessage());
    }

    @Test
    void batchCreateStudents_MixedResults() {
        // 准备测试数据
        List<AdminStudentCreateRequest> requests = new ArrayList<>();

        // 有效请求
        AdminStudentCreateRequest valid = new AdminStudentCreateRequest();
        valid.setStudentNumber("20240001");
        valid.setRealName("Valid Student");

        // 重复学号（同一批次）
        AdminStudentCreateRequest duplicateInBatch = new AdminStudentCreateRequest();
        duplicateInBatch.setStudentNumber("20240002");
        duplicateInBatch.setRealName("Duplicate In Batch");

        // 空请求
        AdminStudentCreateRequest empty = new AdminStudentCreateRequest();

        requests.add(valid);
        requests.add(duplicateInBatch);
        requests.add(duplicateInBatch); // 重复学号
        requests.add(empty);

        // 模拟依赖
        when(studentMapper.existsByStudentNumber("20240001")).thenReturn(false);
        when(userMapper.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.insertUser(any())).thenReturn(1);
        when(studentMapper.insertStudent(any())).thenReturn(1);

        // 执行方法
        Map<String, Object> result = adminStudentService.batchCreateStudents(requests);

        // 验证结果
        assertEquals(1, result.get("successCount")); // 只有1个成功
        assertEquals(3, result.get("failCount"));    // 3个失败（包括空行）

        List<Map<String, String>> failDetails = (List<Map<String, String>>) result.get("failDetails");
        assertEquals(3, failDetails.size());
        assertTrue(failDetails.get(0).get("reason").contains("学号重复"));
        assertEquals("空行跳过", failDetails.get(2).get("reason"));
    }

    @Test
    void importStudents_Success() throws IOException {
        // 模拟Excel解析
        List<AdminStudentCreateRequest> mockRequests = Collections.singletonList(
                new AdminStudentCreateRequest()
        );
        when(ExcelUtils.parseStudentExcel(file)).thenReturn(mockRequests);

        // 模拟批量创建
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("successCount", 1);
        doReturn(expectedResult).when(adminStudentService).batchCreateStudents(mockRequests);

        Map<String, Object> result = adminStudentService.importStudents(file);
        assertEquals(1, result.get("successCount"));
    }

    @Test
    void importStudents_ParseFailure() throws IOException {
        when(ExcelUtils.parseStudentExcel(file)).thenThrow(new IOException("Invalid format"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminStudentService.importStudents(file)
        );
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("文件解析失败"));
    }

    @Test
    void getStudents_Success() {
        // 模拟查询参数
        AdminStudentQueryRequest query = new AdminStudentQueryRequest();
        query.setPage(1);
        query.setSize(10);
        query.setKeyword("张");
        query.setGrade("2024");

        // 模拟Mapper返回
        Student student = new Student();
        student.setUserId(USER_ID);
        student.setStudentNumber(STUDENT_NUMBER);
        when(studentMapper.findStudentsByCondition(eq("张"), eq("2024"), eq(0), eq(10)))
                .thenReturn(Collections.singletonList(student));

        User user = new User();
        user.setUserId(USER_ID);
        user.setRealName(REAL_NAME);
        when(userMapper.findUserById(USER_ID)).thenReturn(user);
        when(studentMapper.countStudentsByCondition(eq("张"), eq("2024"))).thenReturn(1L);

        // 执行查询
        Map<String, Object> result = adminStudentService.getStudents(query);

        // 验证结果
        assertEquals(1L, result.get("total"));
        List<AdminStudentResponse> students = (List<AdminStudentResponse>) result.get("students");
        assertEquals(1, students.size());
        assertEquals(REAL_NAME, students.get(0).getRealName());
    }

    @Test
    void updateStudent_Success() {
        // 准备请求
        AdminStudentUpdateRequest request = new AdminStudentUpdateRequest();
        request.setRealName("新名字");
        request.setGrade("2025");
        request.setMajor("人工智能");

        // 模拟现有学生
        Student existingStudent = new Student();
        existingStudent.setUserId(USER_ID);
        User existingUser = new User();
        existingUser.setUserId(USER_ID);

        when(studentMapper.selectByStudentNumber(STUDENT_NUMBER)).thenReturn(existingStudent);
        when(userMapper.findUserById(USER_ID)).thenReturn(existingUser);
        when(userMapper.updateUser(any())).thenReturn(1);
        when(studentMapper.updateStudent(any())).thenReturn(1);

        AdminStudentResponse response = adminStudentService.updateStudent(STUDENT_NUMBER, request);

        assertEquals("新名字", response.getRealName());
        assertEquals("2025", response.getGrade());
    }

    @Test
    void updateStudent_NotFound() {
        when(studentMapper.selectByStudentNumber(STUDENT_NUMBER)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminStudentService.updateStudent(STUDENT_NUMBER, new AdminStudentUpdateRequest())
        );
        assertEquals(404, exception.getCode());
        assertEquals("学生不存在", exception.getMessage());
    }

    @Test
    void deleteStudent_Success() {
        Student student = new Student();
        student.setUserId(USER_ID);

        when(studentMapper.selectByStudentNumber(STUDENT_NUMBER)).thenReturn(student);
        when(studentMapper.deleteStudent(USER_ID)).thenReturn(1);
        when(userMapper.deleteUser(USER_ID)).thenReturn(1);

        assertDoesNotThrow(() ->
                adminStudentService.deleteStudent(STUDENT_NUMBER)
        );
    }

    @Test
    void deleteStudent_UserDeleteFailed() {
        Student student = new Student();
        student.setUserId(USER_ID);

        when(studentMapper.selectByStudentNumber(STUDENT_NUMBER)).thenReturn(student);
        when(studentMapper.deleteStudent(USER_ID)).thenReturn(1);
        when(userMapper.deleteUser(USER_ID)).thenReturn(0); // 删除失败

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminStudentService.deleteStudent(STUDENT_NUMBER)
        );
        assertEquals(500, exception.getCode());
        assertEquals("用户信息删除失败", exception.getMessage());
    }
}
