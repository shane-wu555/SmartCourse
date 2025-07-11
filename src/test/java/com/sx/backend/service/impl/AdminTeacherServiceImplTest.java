package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminTeacherResponse;
import com.sx.backend.entity.Teacher;
import com.sx.backend.entity.User;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.TeacherMapper;
import com.sx.backend.mapper.UserMapper;
import com.sx.backend.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class AdminTeacherServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private AdminTeacherServiceImpl adminTeacherService;

    // 测试常量
    private final String EMPLOYEE_NUMBER = "T2024001";
    private final String REAL_NAME = "张教授";
    private final String TITLE = "教授";
    private final String DEPARTMENT = "计算机学院";
    private final String BIO = "资深教授";
    private final String USER_ID = UUID.randomUUID().toString();
    private final String EMAIL = EMPLOYEE_NUMBER + "@neu.edu.cn";

    @BeforeEach
    void setUp() {
        // 公共模拟配置可以放在这里
    }

    @Test
    void createTeacher_Success() {
        // 准备请求
        AdminTeacherCreateRequest request = new AdminTeacherCreateRequest();
        request.setEmployeeNumber(EMPLOYEE_NUMBER);
        request.setRealName(REAL_NAME);
        request.setTitle(TITLE);
        request.setDepartment(DEPARTMENT);
        request.setBio(BIO);

        // 模拟依赖行为
        when(teacherMapper.existsByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(false);
        when(userMapper.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(EMPLOYEE_NUMBER)).thenReturn("encodedPassword");
        when(userMapper.insertUser(any(User.class))).thenReturn(1);
        when(teacherMapper.insertTeacher(any(Teacher.class))).thenReturn(1);

        // 执行方法
        AdminTeacherResponse response = adminTeacherService.createTeacher(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(REAL_NAME, response.getRealName());
        assertEquals(EMPLOYEE_NUMBER, response.getEmployeeNumber());
        assertEquals(TITLE, response.getTitle());
        verify(userMapper, times(1)).insertUser(any(User.class));
        verify(teacherMapper, times(1)).insertTeacher(any(Teacher.class));
    }

    @Test
    void createTeacher_DuplicateEmployeeNumber() {
        AdminTeacherCreateRequest request = new AdminTeacherCreateRequest();
        request.setEmployeeNumber(EMPLOYEE_NUMBER);

        when(teacherMapper.existsByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.createTeacher(request)
        );
        assertEquals(409, exception.getCode());
        assertEquals("工号已存在", exception.getMessage());
    }

    @Test
    void createTeacher_UserInsertFailed() {
        AdminTeacherCreateRequest request = new AdminTeacherCreateRequest();
        request.setEmployeeNumber(EMPLOYEE_NUMBER);
        request.setRealName(REAL_NAME);

        when(teacherMapper.existsByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(false);
        when(userMapper.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.insertUser(any(User.class))).thenReturn(0); // 插入失败

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.createTeacher(request)
        );
        assertEquals(500, exception.getCode());
        assertEquals("用户创建失败", exception.getMessage());
    }

    @Test
    void importTeachers_Success() throws IOException {
        // 创建有效的 Excel 文件内容
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Teachers");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"工号", "姓名", "职称", "院系", "简介"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // 创建数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(EMPLOYEE_NUMBER);
        dataRow.createCell(1).setCellValue(REAL_NAME);
        dataRow.createCell(2).setCellValue(TITLE);
        dataRow.createCell(3).setCellValue(DEPARTMENT);
        dataRow.createCell(4).setCellValue(BIO);

        // 将工作簿转换为字节流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        byte[] bytes = bos.toByteArray();

        // 模拟文件输入流 - 这是关键修改点
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

        // 不再模拟 parseTeacherExcel() 方法
        // 让服务方法自然调用 ExcelUtils 解析真实流

        // 模拟批量创建
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("successCount", 1);
        expectedResult.put("failCount", 0);

        // 使用 spy 部分模拟服务实例
        AdminTeacherServiceImpl serviceSpy = spy(adminTeacherService);
        doReturn(expectedResult).when(serviceSpy).batchCreateTeachers(anyList());

        // 执行测试
        Map<String, Object> result = serviceSpy.importTeachers(file);
        assertEquals(1, result.get("successCount"));
    }

    @Test
    void importTeachers_ParseFailure() throws IOException {
        // 模拟文件获取输入流时抛出 IOException
        when(file.getInputStream()).thenThrow(new IOException("Invalid format"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.importTeachers(file)
        );
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("文件解析失败"));
    }

    @Test
    void batchCreateTeachers_MixedResults() {
        // 准备测试数据
        List<AdminTeacherCreateRequest> requests = new ArrayList<>();

        // 有效请求
        AdminTeacherCreateRequest valid = new AdminTeacherCreateRequest();
        valid.setEmployeeNumber("T2024001");
        valid.setRealName("Valid Teacher");

        // 重复工号（同一批次）
        AdminTeacherCreateRequest duplicateInBatch = new AdminTeacherCreateRequest();
        duplicateInBatch.setEmployeeNumber("T2024002");
        duplicateInBatch.setRealName("Duplicate In Batch");

        // 空请求
        AdminTeacherCreateRequest empty = new AdminTeacherCreateRequest();

        requests.add(valid);
        requests.add(duplicateInBatch);
        requests.add(duplicateInBatch); // 重复工号
        requests.add(empty);

        // 模拟依赖
        when(teacherMapper.existsByEmployeeNumber("T2024001")).thenReturn(false);
        when(userMapper.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.insertUser(any())).thenReturn(1);
        when(teacherMapper.insertTeacher(any())).thenReturn(1);

        // 执行方法
        Map<String, Object> result = adminTeacherService.batchCreateTeachers(requests);

        // 验证结果
        assertEquals(1, result.get("successCount")); // 只有1个成功
        assertEquals(3, result.get("failCount"));    // 3个失败（包括空行）

        List<Map<String, String>> failDetails = (List<Map<String, String>>) result.get("failDetails");
        assertEquals(3, failDetails.size());
        assertTrue(failDetails.get(0).get("reason").contains("工号重复"));
    }

    @Test
    void getTeachers_Success() {
        // 模拟查询参数
        AdminTeacherQueryRequest query = new AdminTeacherQueryRequest();
        query.setPage(1);
        query.setSize(10);
        query.setKeyword("张");
        query.setDepartment("计算机学院");

        // 模拟Mapper返回
        Teacher teacher = new Teacher();
        teacher.setUserId(USER_ID);
        teacher.setEmployeeNumber(EMPLOYEE_NUMBER);
        when(teacherMapper.findTeachersByCondition(eq("张"), eq("计算机学院"), eq(0), eq(10)))
                .thenReturn(Collections.singletonList(teacher));

        User user = new User();
        user.setUserId(USER_ID);
        user.setRealName(REAL_NAME);
        when(userMapper.findUserById(USER_ID)).thenReturn(user);
        when(teacherMapper.countTeachersByCondition(eq("张"), eq("计算机学院"))).thenReturn(1L);

        // 执行查询
        Map<String, Object> result = adminTeacherService.getTeachers(query);

        // 验证结果
        assertEquals(1L, result.get("total"));
        List<AdminTeacherResponse> teachers = (List<AdminTeacherResponse>) result.get("teachers");
        assertEquals(1, teachers.size());
        assertEquals(REAL_NAME, teachers.get(0).getRealName());
    }

    @Test
    void updateTeacher_Success() {
        // 准备请求
        AdminTeacherUpdateRequest request = new AdminTeacherUpdateRequest();
        request.setRealName("李教授");
        request.setTitle("副教授");
        request.setDepartment("人工智能学院");
        request.setBio("新研究方向");

        // 模拟现有教师
        Teacher existingTeacher = new Teacher();
        existingTeacher.setUserId(USER_ID);
        existingTeacher.setEmployeeNumber(EMPLOYEE_NUMBER);
        User existingUser = new User();
        existingUser.setUserId(USER_ID);
        existingUser.setRealName(REAL_NAME);

        when(teacherMapper.findByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(existingTeacher);
        when(userMapper.findUserById(USER_ID)).thenReturn(existingUser);
        when(userMapper.updateUser(any())).thenReturn(1);
        when(teacherMapper.updateTeacher(any())).thenReturn(1);

        AdminTeacherResponse response = adminTeacherService.updateTeacher(EMPLOYEE_NUMBER, request);

        assertEquals("李教授", response.getRealName());
        assertEquals("副教授", response.getTitle());
        assertEquals("人工智能学院", response.getDepartment());
    }

    @Test
    void updateTeacher_NotFound() {
        when(teacherMapper.findByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.updateTeacher(EMPLOYEE_NUMBER, new AdminTeacherUpdateRequest())
        );
        assertEquals(404, exception.getCode());
        assertEquals("教师不存在", exception.getMessage());
    }

    @Test
    void deleteTeacher_Success() {
        Teacher teacher = new Teacher();
        teacher.setUserId(USER_ID);

        when(teacherMapper.findByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(teacher);
        when(teacherMapper.deleteTeacher(USER_ID)).thenReturn(1);
        when(userMapper.deleteUser(USER_ID)).thenReturn(1);

        assertDoesNotThrow(() ->
                adminTeacherService.deleteTeacher(EMPLOYEE_NUMBER)
        );
    }

    @Test
    void deleteTeacher_UserDeleteFailed() {
        Teacher teacher = new Teacher();
        teacher.setUserId(USER_ID);

        when(teacherMapper.findByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(teacher);
        when(teacherMapper.deleteTeacher(USER_ID)).thenReturn(1);
        when(userMapper.deleteUser(USER_ID)).thenReturn(0); // 删除失败

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.deleteTeacher(EMPLOYEE_NUMBER)
        );
        assertEquals(500, exception.getCode());
        assertEquals("用户信息删除失败", exception.getMessage());
    }

    @Test
    void deleteTeacher_TeacherNotFound() {
        when(teacherMapper.findByEmployeeNumber(EMPLOYEE_NUMBER)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminTeacherService.deleteTeacher(EMPLOYEE_NUMBER)
        );
        assertEquals(404, exception.getCode());
        assertEquals("教师不存在", exception.getMessage());
    }
}
