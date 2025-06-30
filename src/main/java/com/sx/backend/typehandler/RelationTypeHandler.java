package com.sx.backend.typehandler;

import com.sx.backend.entity.RelationType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RelationType 枚举类型处理器
 * 用于处理 RelationType 枚举与数据库字符串的转换
 */
@MappedTypes(RelationType.class)
public class RelationTypeHandler extends BaseTypeHandler<RelationType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RelationType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public RelationType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : RelationType.valueOf(value);
    }

    @Override
    public RelationType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : RelationType.valueOf(value);
    }

    @Override
    public RelationType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : RelationType.valueOf(value);
    }
}
