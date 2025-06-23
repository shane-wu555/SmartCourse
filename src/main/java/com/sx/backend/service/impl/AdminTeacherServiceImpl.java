// AdminTeacherServiceImpl.java
package com.sx.backend.service.impl;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminTeacherResponse;
import com.sx.backend.entity.Teacher;
import com.sx.backend.entity.User;
import com.sx.backend.entity.Role;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.TeacherMapper;
import com.sx.backend.mapper.UserMapper;
import com.sx.backend.service.AdminTeacherService;
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
public class AdminTeacherServiceImpl implements AdminTeacherService {

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminTeacherServiceImpl(
            UserMapper userMapper,
            TeacherMapper teacherMapper,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.teacherMapper = teacherMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AdminTeacherResponse createTeacher(AdminTeacherCreateRequest request) {
        // 检查工号是否已存在
        if (teacherMapper.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new BusinessException(409, "工号已存在");
        }

        // 检查邮箱是否已存在
        String email = request.getEmployeeNumber() + "@neu.edu.cn";
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(409, "邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getEmployeeNumber()));
        user.setEmail(email);
        user.setRealName(request.getRealName());
        user.setRole(Role.TEACHER);
        user.setRegisterTime(LocalDateTime.now());
        user.setAvatar("default-avatar.png");

        // 插入用户
        if (userMapper.insertUser(user) != 1) {
            throw new BusinessException(500, "用户创建失败");
        }

        // 创建教师
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUserId());
        teacher.setEmployeeNumber(request.getEmployeeNumber());
        teacher.setTitle(request.getTitle());
        teacher.setDepartment(request.getDepartment());
        teacher.setBio(request.getBio());

        // 插入教师
        if (teacherMapper.insertTeacher(teacher) != 1) {
            throw new BusinessException(500, "教师信息创建失败");
        }

        return buildTeacherResponse(user, teacher);
    }

    @Override
    @Transactional
    public Map<String, Object> importTeachers(MultipartFile file) {
        try {
            // 解析Excel文件
            List<AdminTeacherCreateRequest> requests = ExcelUtils.parseTeacherExcel(file);
            return batchCreateTeachers(requests);
        } catch (IOException e) {
            throw new BusinessException(400, "文件解析失败: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> batchCreateTeachers(List<AdminTeacherCreateRequest> requests) {
        int successCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();
        Set<String> processedNumbers = new HashSet<>();

        for (AdminTeacherCreateRequest request : requests) {
            try {
                // 跳过空行
                if (isRequestEmpty(request)) continue;

                // 检查当前批次内的重复工号
                if (!processedNumbers.add(request.getEmployeeNumber())) {
                    throw new BusinessException(400, "工号重复: " + request.getEmployeeNumber());
                }

                // 创建教师
                createTeacher(request);
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
    public Map<String, Object> getTeachers(AdminTeacherQueryRequest queryRequest) {
        // 计算分页偏移量
        int offset = (queryRequest.getPage() - 1) * queryRequest.getSize();

        // 查询教师列表
        List<Teacher> teachers = teacherMapper.findTeachersByCondition(
                queryRequest.getKeyword(),
                queryRequest.getDepartment(),
                offset,
                queryRequest.getSize()
        );

        // 查询总数
        long total = teacherMapper.countTeachersByCondition(
                queryRequest.getKeyword(),
                queryRequest.getDepartment()
        );

        // 构建响应
        List<AdminTeacherResponse> responses = new ArrayList<>();
        for (Teacher teacher : teachers) {
            User user = userMapper.findUserById(teacher.getUserId());
            responses.add(buildTeacherResponse(user, teacher));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("teachers", responses);
        return result;
    }

    @Override
    @Transactional
    public AdminTeacherResponse updateTeacher(String employeeNumber, AdminTeacherUpdateRequest request) {
        // 根据工号查询教师
        Teacher teacher = teacherMapper.findByEmployeeNumber(employeeNumber);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }

        User user = userMapper.findUserById(teacher.getUserId());
        if (user == null) {
            throw new BusinessException(404, "关联用户不存在");
        }

        // 更新用户信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
            user.setUsername(request.getRealName());
            if (userMapper.updateUser(user) != 1) {
                throw new BusinessException(500, "用户信息更新失败");
            }
        }

        // 更新教师信息
        if (request.getTitle() != null) teacher.setTitle(request.getTitle());
        if (request.getDepartment() != null) teacher.setDepartment(request.getDepartment());
        if (request.getBio() != null) teacher.setBio(request.getBio());

        if (teacherMapper.updateTeacher(teacher) != 1) {
            throw new BusinessException(500, "教师信息更新失败");
        }

        return buildTeacherResponse(user, teacher);
    }

    @Override
    @Transactional
    public void deleteTeacher(String employeeNumber) {
        // 根据工号查询教师
        Teacher teacher = teacherMapper.findByEmployeeNumber(employeeNumber);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }
        String teacherId = teacher.getUserId();

        // 先删除教师记录
        if (teacherMapper.deleteTeacher(teacherId) != 1) {
            throw new BusinessException(500, "教师信息删除失败");
        }

        // 再删除用户记录
        if (userMapper.deleteUser(teacherId) != 1) {
            throw new BusinessException(500, "用户信息删除失败");
        }
    }

    private AdminTeacherResponse buildTeacherResponse(User user, Teacher teacher) {
        AdminTeacherResponse response = new AdminTeacherResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setEmployeeNumber(teacher.getEmployeeNumber());
        response.setTitle(teacher.getTitle());
        response.setDepartment(teacher.getDepartment());
        response.setBio(teacher.getBio());
        response.setEmail(user.getEmail());
        return response;
    }

    private boolean isRequestEmpty(AdminTeacherCreateRequest request) {
        return (request.getRealName() == null || request.getRealName().trim().isEmpty()) &&
                (request.getEmployeeNumber() == null || request.getEmployeeNumber().trim().isEmpty());
    }

    private void addFailureDetail(List<Map<String, String>> failDetails,
                                  AdminTeacherCreateRequest request,
                                  String reason) {
        Map<String, String> failInfo = new HashMap<>();
        failInfo.put("realName", request.getRealName());
        failInfo.put("employeeNumber", request.getEmployeeNumber());
        failInfo.put("reason", reason);
        failDetails.add(failInfo);
    }
}