package com.sx.backend.service;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.TestPaper;

/**
 * 智能组卷服务接口
 */
public interface TestPaperService {
    /**
     * 根据请求参数生成试卷
     * @param requestDTO 组卷请求参数
     * @return 生成的试卷对象
     */
    TestPaper generatePaper(GeneratePaperRequestDTO requestDTO);
}
