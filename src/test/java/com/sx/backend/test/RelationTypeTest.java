package com.sx.backend.test;

import com.sx.backend.entity.RelationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试RelationType枚举
 */
public class RelationTypeTest {
    
    @Test
    public void testEnumValues() {
        // 测试枚举值是否正确
        assertEquals("PREREQUISITE", RelationType.PREREQUISITE.name());
        assertEquals("RELATED", RelationType.RELATED.name());
        assertEquals("PART_OF", RelationType.PART_OF.name());
        
        // 测试valueOf方法
        assertEquals(RelationType.PREREQUISITE, RelationType.valueOf("PREREQUISITE"));
        assertEquals(RelationType.RELATED, RelationType.valueOf("RELATED"));
        assertEquals(RelationType.PART_OF, RelationType.valueOf("PART_OF"));
    }
    
    @Test
    public void testEnumCount() {
        // 确保只有3个枚举值
        assertEquals(3, RelationType.values().length);
    }
}
