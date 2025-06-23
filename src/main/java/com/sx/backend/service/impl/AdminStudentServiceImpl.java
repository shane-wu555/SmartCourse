package com.sx.backend.service.impl;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminStudentResponse;
import com.sx.backend.entity.Student;
import com.sx.backend.entity.User;
import com.sx.backend.entity.Role;
import com.sx.backend.mapper.StudentMapper;
import com.sx.backend.mapper.UserMapper;
import com.sx.backend.service.AdminStudentService;

import com.sx.backend.util.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminStudentServiceImpl implements AdminStudentService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminStudentServiceImpl(
            UserMapper userMapper,
            StudentMapper studentMapper,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AdminStudentResponse createStudent(AdminStudentCreateRequest request) {
        // 检查学号是否已存在
        if (studentMapper.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(409, "学号已存在"); // 使用409表示冲突
        }

        // 检查邮箱是否已存在
        String email = request.getStudentNumber() + "@neu.edu.cn";
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(409, "邮箱已存在"); // 使用409表示冲突
        }

        // 创建用户
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getStudentNumber()));
        user.setEmail(email);
        user.setRealName(request.getRealName());
        user.setRole(Role.STUDENT);
        user.setRegisterTime(LocalDateTime.now());
        user.setAvatar("default-avatar.png");

        // 插入用户
        if (userMapper.insertUser(user) != 1) {
            throw new BusinessException(500, "用户创建失败");
        }

        // 创建学生
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setStudentNumber(request.getStudentNumber());
        student.setGrade(request.getGrade());
        student.setMajor(request.getMajor());

        // 插入学生
        if (studentMapper.insertStudent(student) != 1) {
            throw new BusinessException(500, "学生信息创建失败");
        }

        return buildStudentResponse(user, student);
    }

    @Override
    @Transactional
    public Map<String, Object> importStudents(MultipartFile file) {
        try {
            // 解析Excel文件
            List<AdminStudentCreateRequest> requests = ExcelUtils.parseStudentExcel(file);
            return batchCreateStudents(requests);
        } catch (IOException e) {
            throw new BusinessException(400, "文件解析失败: " + e.getMessage());
        }
    }


    @Transactional
    public Map<String, Object> batchCreateStudents(List<AdminStudentCreateRequest> requests) {
        int successCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();
        Set<String> processedNumbers = new HashSet<>();

        for (AdminStudentCreateRequest request : requests) {
            try {
                // 跳过空行
                if (isRequestEmpty(request)) continue;

                // 检查当前批次内的重复学号
                if (!processedNumbers.add(request.getStudentNumber())) {
                    throw new BusinessException(400, "学号重复: " + request.getStudentNumber());
                }

                // 创建学生
                createStudent(request);
                successCount++;
            } catch (BusinessException e) {
                addFailureDetail(failDetails, request, e.getMessage());
            } catch (Exception e) {
                addFailureDetail(failDetails, request, "系统错误: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", requests.size() - successCount);
        result.put("failDetails", failDetails);
        return result;
    }

    @Override
    public Map<String, Object> getStudents(AdminStudentQueryRequest queryRequest) {
        // 计算分页偏移量
        int offset = (queryRequest.getPage() - 1) * queryRequest.getSize();

        // 查询学生列表
        List<Student> students = studentMapper.findStudentsByCondition(
                queryRequest.getKeyword(),
                queryRequest.getGrade(),
                offset,
                queryRequest.getSize()
        );

        // 查询总数
        long total = studentMapper.countStudentsByCondition(
                queryRequest.getKeyword(),
                queryRequest.getGrade()
        );

        // 构建响应
        List<AdminStudentResponse> responses = new ArrayList<>();
        for (Student student : students) {
            User user = userMapper.findUserById(student.getUserId());
            responses.add(buildStudentResponse(user, student));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("students", responses);
        return result;
    }

    @Override
    @Transactional
    public AdminStudentResponse updateStudent(String studentNumber, AdminStudentUpdateRequest request) {
        // 根据学号查询学生
        Student student = studentMapper.selectByStudentNumber(studentNumber);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        User user = userMapper.findUserById(student.getUserId());
        if (user == null) {
            throw new BusinessException(404, "关联用户不存在");
        }

        // 更新用户信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
            user.setUsername(request.getRealName());
        }
        if (userMapper.updateUser(user) != 1) {
            throw new BusinessException(500, "用户信息更新失败");
        }

        // 更新学生信息
        if (request.getGrade() != null) student.setGrade(request.getGrade());
        if (request.getMajor() != null) student.setMajor(request.getMajor());
        if (studentMapper.updateStudent(student) != 1) {
            throw new BusinessException(500, "学生信息更新失败");
        }

        return buildStudentResponse(user, student);
    }

    // 修改删除学生的方法
    @Override
    @Transactional
    public void deleteStudent(String studentNumber) {
        // 根据学号查询学生
        Student student = studentMapper.selectByStudentNumber(studentNumber);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        String studentId = student.getUserId();

        // 先删除学生记录
        if (studentMapper.deleteStudent(studentId) != 1) {
            throw new BusinessException(500, "学生信息删除失败");
        }

        // 再删除用户记录
        if (userMapper.deleteUser(studentId) != 1) {
            throw new BusinessException(500, "用户信息删除失败");
        }
    }

    private AdminStudentResponse buildStudentResponse(User user, Student student) {
        AdminStudentResponse response = new AdminStudentResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setStudentNumber(student.getStudentNumber());
        response.setGrade(student.getGrade());
        response.setMajor(student.getMajor());
        response.setEmail(user.getEmail());
        return response;
    }

    private void validateStudentRequest(AdminStudentCreateRequest request) {
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            throw new BusinessException(400, "真实姓名不能为空");
        }

        if (request.getStudentNumber() == null || request.getStudentNumber().trim().isEmpty()) {
            throw new BusinessException(400, "学号不能为空");
        }

        if (studentMapper.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(400, "学号已存在: " + request.getStudentNumber());
        }

        String email = request.getStudentNumber() + "@neu.edu.cn";
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(400, "邮箱已存在: " + email);
        }
    }

    private boolean isRequestEmpty(AdminStudentCreateRequest request) {
        return (request.getRealName() == null || request.getRealName().trim().isEmpty()) &&
                (request.getStudentNumber() == null || request.getStudentNumber().trim().isEmpty());
    }

    private void addFailureDetail(List<Map<String, String>> failDetails,
                                  AdminStudentCreateRequest request,
                                  String reason) {
        Map<String, String> failInfo = new HashMap<>();
        failInfo.put("realName", request.getRealName());
        failInfo.put("studentNumber", request.getStudentNumber());
        failInfo.put("reason", reason);
        failDetails.add(failInfo);
    }
}
