package com.sx.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // 这个测试验证Spring上下文能够正常加载
        // 在测试环境下运行，减少资源消耗
    }

}
