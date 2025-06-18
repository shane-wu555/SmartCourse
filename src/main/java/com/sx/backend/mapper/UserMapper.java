package com.sx.backend.mapper;

import com.sx.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    int insertUser(User user);

    int updateLastLoginTime(
            @Param("userId") String userId,
            @Param("lastLoginTime") LocalDateTime lastLoginTime
    );
}