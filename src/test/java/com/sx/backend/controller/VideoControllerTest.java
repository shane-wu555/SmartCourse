package com.sx.backend.controller;

import com.sx.backend.BackendApplication;
import com.sx.backend.mapper.ResourceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceMapper resourceMapper;

    @Test
    void testDebugResource_NotFound() throws Exception {
        mockMvc.perform(get("/api/debug/resource/invalid-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Resource not found in database"));
    }

    @Test
    void testGetVideo_NotFound() throws Exception {
        mockMvc.perform(get("/api/video/invalid-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetVideoAlternative_AlsoNotFound() throws Exception {
        // 模拟不存在的资源
        when(resourceMapper.getResourceById("invalid-id")).thenReturn(null);

        mockMvc.perform(get("/api/videos/invalid-id"))
                .andExpect(status().isNotFound());
    }
}
