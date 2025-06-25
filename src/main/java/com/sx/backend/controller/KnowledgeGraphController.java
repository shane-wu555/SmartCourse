package com.sx.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.service.KnowledgeGraphService;

@RestController
@RequestMapping("/api/knowledge-graph")
public class KnowledgeGraphController {
    private final KnowledgeGraphService service = new KnowledgeGraphService();

    @PostMapping("/generate")
    public KnowledgeGraphDTO generate(@RequestParam("file") MultipartFile file) throws Exception {
        String content = new String(file.getBytes(), "UTF-8");
        return service.generateGraph(content);
    }

    @PostMapping("/generateByText")
    public KnowledgeGraphDTO generateByText(@RequestBody String content) throws Exception {
        return service.generateGraph(content);
    }
}
