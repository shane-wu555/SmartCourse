package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.entity.Course;
import com.sx.backend.exception.AccessDeniedException;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private static final Logger log = LoggerFactory.getLogger(KnowledgePointServiceImpl.class);

    @Override
    public List<CourseDTO> getCoursesByTeacherId(String teacherId) {
        // 直接查询DTO列表，避免转换
        return courseMapper.findByTeacherId(teacherId);
    }

    @Override
    public PageResult<CourseDTO> getCoursesByPage(String teacherId, int page, int size, String semester, String keyword) {
        int offset = (page - 1) * size;
        List<CourseDTO> dtoList = courseMapper.findByTeacherIdWithPaging(teacherId, offset, size, semester, keyword);
        long total = courseMapper.countByTeacherIdWithPaging(teacherId, semester, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResult<>(page, size, total, totalPages, dtoList);
    }

    @Override
    public CourseDTO createCourse(CourseCreateRequest request, String teacherId) {
        // 验证课程编号唯一性
        if (courseMapper.countByCourseCode(request.getCourseCode(), null) > 0) {
            throw new BusinessException(409, "课程编号已存在");
        }

        Course course = new Course();
        course.setCourseId(UUID.randomUUID().toString());
        course.setCourseCode(request.getCourseCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setHours(request.getHours());
        course.setSemester(request.getSemester());
        course.setTeacherId(teacherId); // 直接设置teacherId

        courseMapper.insert(course);

        // 返回新创建的课程DTO
        return courseMapper.findById(course.getCourseId());
    }

    @Override
    public CourseDTO getCourseDetail(String courseId, String teacherId) {
        CourseDTO courseDTO = courseMapper.findById(courseId);
        if (courseDTO == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(courseDTO.getTeacherId())) {
            throw new BusinessException(403, "无权访问此课程");
        }
        return courseDTO;
    }

    @Override
    public CourseDTO updateCourse(String courseId, CourseUpdateRequest request, String teacherId) {
        CourseDTO existingCourse = courseMapper.findById(courseId);
        if (existingCourse == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(existingCourse.getTeacherId())) {
            throw new BusinessException(403, "无权修改此课程");
        }
        // 验证课程编号唯一性（排除自身）
        if (courseMapper.countByCourseCode(request.getCourseCode(), courseId) > 0) {
            throw new BusinessException(409, "课程编号已存在");
        }

        // 创建更新对象
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseCode(request.getCourseCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setHours(request.getHours());
        course.setSemester(request.getSemester());

        courseMapper.update(course);

        // 返回更新后的课程DTO
        return courseMapper.findById(courseId);
    }

    @Override
    public void deleteCourse(String courseId, String teacherId) {
        CourseDTO courseDTO = courseMapper.findById(courseId);
        if (courseDTO == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 验证教师权限
        if (!teacherId.equals(courseDTO.getTeacherId())) {
            throw new BusinessException(403, "无权删除此课程");
        }
        // 检查关联数据
        if (courseMapper.countEnrollmentsByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联学生，无法删除");
        }
        if (courseMapper.countTasksByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联任务，无法删除");
        }
        if (courseMapper.countResourcesByCourseId(courseId) > 0) {
            throw new BusinessException(409, "课程存在关联资源，无法删除");
        }

        courseMapper.delete(courseId);
    }

    public String getCurrentTeacherId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 记录详细的认证信息
        if (authentication != null) {
            log.info("安全上下文认证信息: [认证状态: {}, 类型: {}, 名称: {}]",
                    authentication.isAuthenticated(),
                    authentication.getClass().getSimpleName(),
                    authentication.getName());

            log.info("认证主体: {}", authentication.getPrincipal().toString());
            log.info("授权信息: {}", authentication.getAuthorities().toString());
        } else {
            log.warn("安全上下文无认证信息！");
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("未认证用户: 无法获取教师ID");
            throw new AccessDeniedException("未认证用户");
        }

        // 尝试从不同来源获取教师ID
        Object principal = authentication.getPrincipal();

        // 情况1：Principal是字符串（直接是教师ID）
        if (principal instanceof String) {
            String teacherId = (String) principal;
            log.info("从String类型Principal获取教师ID: {}", teacherId);
            return teacherId;
        }

        // 情况2：Principal是UserDetails实现
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            log.info("从UserDetails获取教师ID: {}", username);
            return username;
        }

        // 情况3：Principal是自定义用户对象（例如实现了UserDetails）
        if (principal instanceof com.sx.backend.entity.User) {
            String userId = ((com.sx.backend.entity.User) principal).getUserId();
            log.info("从自定义User对象获取教师ID: {}", userId);
            return userId;
        }

        // 情况4：JWT声明信息
        if (principal instanceof io.jsonwebtoken.Claims) {
            String subject = ((io.jsonwebtoken.Claims) principal).getSubject();
            log.info("从JWT Claims获取教师ID: {}", subject);
            return subject;
        }

        // 无法识别的Principal类型
        log.error("无法识别的Principal类型: {}", principal.getClass().getName());
        throw new AccessDeniedException("无法获取教师ID - 不支持的Principal类型");
    }

    @Override
    public Course getCourseEntityById(String courseId) {
        // 1. 查询课程DTO
        CourseDTO courseDTO = courseMapper.findById(courseId);

        if (courseDTO == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 2. 将DTO转换为Entity - 只复制基本字段（不包括关联集合）
        Course course = new Course();
        course.setCourseId(courseDTO.getCourseId());
        course.setCourseCode(courseDTO.getCourseCode());
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setCredit(courseDTO.getCredit());
        course.setHours(courseDTO.getHours());
        course.setSemester(courseDTO.getSemester());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setCreateTime(courseDTO.getCreateTime());
        course.setUpdateTime(courseDTO.getUpdateTime());
        return course;
    }
}