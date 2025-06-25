package com.sx.backend.controller;

import com.sx.backend.dto.GeneratePaperRequestDTO;
import com.sx.backend.entity.TestPaper;
import com.sx.backend.service.TestPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//TODO 接口暂未进行验证
@RestController
@RequestMapping("/api/paper")
public class TestPaperController {
    @Autowired
    private TestPaperService testPaperService;

    /**
     * 智能组卷接口
     * @param requestDTO 组卷请求参数
     * @return 生成的试卷对象
     */
    @PostMapping("/generate")
    public TestPaper generatePaper(@RequestBody GeneratePaperRequestDTO requestDTO) {
        return testPaperService.generatePaper(requestDTO);
    }
}
