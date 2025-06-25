package com.sx.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

public class FileTextExtractService {
    public String extractTextFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new IllegalArgumentException("文件名不能为空");
        if (filename.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (filename.endsWith(".docx") || filename.endsWith(".doc")) {
            return extractTextFromWord(file);
        } else if (filename.endsWith(".pptx") || filename.endsWith(".ppt")) {
            return extractTextFromPpt(file);
        } else {
            // 纯文本
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws Exception {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromWord(MultipartFile file) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractTextFromPpt(MultipartFile file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XMLSlideShow ppt = new XMLSlideShow(file.getInputStream())) {
            for (XSLFSlide slide : ppt.getSlides()) {
                if (slide.getTitle() != null) sb.append(slide.getTitle()).append("\n");
                if (slide.getNotes() != null) sb.append(slide.getNotes()).append("\n");
                // 可遍历 shape 提取更多内容
            }
        }
        return sb.toString();
    }
}
