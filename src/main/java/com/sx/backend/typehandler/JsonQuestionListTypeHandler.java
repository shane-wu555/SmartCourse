package com.sx.backend.typehandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sx.backend.entity.Question;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于处理 List<Question> 类型与 JSON 字符串之间的转换
 */
public class JsonQuestionListTypeHandler extends BaseTypeHandler<List<Question>> {
    
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        // 配置ObjectMapper忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 只序列化非空属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 禁用写入getter方法产生的属性
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Question> parameter, JdbcType jdbcType) throws SQLException {
        try {
            if (parameter == null || parameter.isEmpty()) {
                ps.setString(i, "[]");
            } else {
                String json = objectMapper.writeValueAsString(parameter);
                ps.setString(i, json);
            }
        } catch (Exception e) {
            throw new SQLException("Error converting List<Question> to JSON string", e);
        }
    }

    @Override
    public List<Question> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<Question> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<Question> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }
    
    private List<Question> parseJson(String json) throws SQLException {
        try {
            if (json == null || json.trim().isEmpty() || "null".equals(json)) {
                return new ArrayList<>();
            }
            
            // 尝试解析为完整的题目对象数组
            return objectMapper.readValue(json, new TypeReference<List<Question>>() {});
            
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON string to List<Question>: " + e.getMessage(), e);
        }
    }
}
