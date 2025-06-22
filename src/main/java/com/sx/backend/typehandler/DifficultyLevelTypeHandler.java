package com.sx.backend.typehandler;

import com.sx.backend.entity.DifficultyLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DifficultyLevelTypeHandler extends BaseTypeHandler<DifficultyLevel> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    DifficultyLevel parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public DifficultyLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : DifficultyLevel.valueOf(value);
    }

    @Override
    public DifficultyLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : DifficultyLevel.valueOf(value);
    }

    @Override
    public DifficultyLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : DifficultyLevel.valueOf(value);
    }
}