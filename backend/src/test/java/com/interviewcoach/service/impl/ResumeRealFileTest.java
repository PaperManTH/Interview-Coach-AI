package com.interviewcoach.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.entity.ResumeProfile;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.mapper.ResumeProfileMapper;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.util.ResumeParserUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 使用真实个人简历 PDF 进行端到端解析测试。
 */
class ResumeRealFileTest {

    private static final Path RESUME_DIR = Paths.get(
            "C:\\Users\\thpaperman\\Desktop\\实习\\个人简历");

    private ResumeProfileMapper mapper;
    private SpringAiService aiService;
    private ResumeServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = mock(ResumeProfileMapper.class);
        aiService = mock(SpringAiService.class);
        objectMapper = new ObjectMapper();
        service = new ResumeServiceImpl(mapper, aiService, objectMapper);
    }

    // ==================== PDF 文本提取 ====================

    @Test
    void shouldExtractTextFromRealPdf() throws IOException {
        Path pdfFile = findFirstPdf();
        assertTrue(Files.exists(pdfFile), "PDF 文件不存在: " + pdfFile);

        byte[] bytes = Files.readAllBytes(pdfFile);
        MockMultipartFile file = new MockMultipartFile(
                "file", pdfFile.getFileName().toString(),
                "application/pdf", bytes);

        String text = ResumeParserUtils.parse(file);
        assertNotNull(text);
        assertFalse(text.isBlank(), "PDF 文本提取结果为空");

        System.out.println("\n==================== PDF 文本提取结果 ====================");
        System.out.println("文件: " + pdfFile.getFileName());
        System.out.println("大小: " + bytes.length + " bytes");
        System.out.println("字符数: " + text.length());
        System.out.println("------------------------------------------------------------");
        System.out.println(text);
        System.out.println("============================================================");
    }

    // ==================== 完整流程（mock LLM） ====================

    @Test
    void shouldFullFlowWithRealPdf() throws IOException {
        Path pdfFile = findFirstPdf();
        assertTrue(Files.exists(pdfFile));

        byte[] bytes = Files.readAllBytes(pdfFile);
        MockMultipartFile file = new MockMultipartFile(
                "file", pdfFile.getFileName().toString(),
                "application/pdf", bytes);

        // mock LLM 返回结构化 JSON
        String llmJson = """
                {
                  "candidateSummary": "后端开发工程师，具有丰富的Java和Spring Boot开发经验",
                  "skills": ["Java", "Spring Boot", "MySQL", "Redis", "Docker", "MyBatis"],
                  "projects": ["电商系统后端开发", "微服务架构迁移"],
                  "workExperience": ["某公司后端开发实习生", "独立完成多个后端项目"],
                  "strengths": ["扎实的Java基础", "熟悉微服务架构", "良好的编码习惯"],
                  "possibleQuestions": [
                    "请介绍一下你的Spring Boot项目经验",
                    "Redis在你的项目中是如何使用的？",
                    "你如何处理数据库优化？"
                  ]
                }
                """;
        when(aiService.chatWithSystem(anyString(), anyString())).thenReturn(llmJson);

        doAnswer(inv -> {
            ResumeProfile p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        }).when(mapper).insert(any(ResumeProfile.class));

        ResumeResponse resp = service.uploadAndParse(file, "real-user");

        System.out.println("\n==================== 完整流程测试结果 ====================");
        System.out.println("文件: " + pdfFile.getFileName());
        System.out.println("ID: " + resp.getId());
        System.out.println("文本预览 (前200字): " + resp.getResumeTextPreview().substring(
                0, Math.min(resp.getResumeTextPreview().length(), 200)));
        System.out.println("\n--- LLM 结构化解析 ---");
        System.out.println("候选人概览: " + resp.getParsedData().getCandidateSummary());
        System.out.println("技能: " + String.join(", ", resp.getParsedData().getSkills()));
        System.out.println("可能的问题:");
        for (String q : resp.getParsedData().getPossibleQuestions()) {
            System.out.println("  - " + q);
        }
        System.out.println("============================================================");

        assertNotNull(resp);
        assertEquals(100L, resp.getId());
        assertNotNull(resp.getParsedData());
        assertFalse(resp.getParsedData().getSkills().isEmpty());
        assertFalse(resp.getParsedData().getPossibleQuestions().isEmpty());
    }

    // ==================== 逐文件解析 ====================

    @Test
    void shouldParseAllResumeFiles() throws IOException {
        assertTrue(Files.isDirectory(RESUME_DIR), "简历目录不存在: " + RESUME_DIR);

        Path[] pdfs;
        try (var stream = Files.list(RESUME_DIR)) {
            pdfs = stream
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .sorted()
                    .toArray(Path[]::new);
        }

        assertTrue(pdfs.length > 0, "目录中没有 PDF 文件");

        System.out.println("\n==================== 批量解析所有简历 ====================");
        for (Path pdf : pdfs) {
            byte[] bytes = Files.readAllBytes(pdf);
            MockMultipartFile file = new MockMultipartFile(
                    "file", pdf.getFileName().toString(),
                    "application/pdf", bytes);

            String text = ResumeParserUtils.parse(file);
            System.out.println("\n[" + pdf.getFileName() + "]");
            System.out.println("  大小: " + bytes.length + " bytes, 字符数: " + text.length());
            System.out.println("  前100字: " + text.substring(0, Math.min(text.length(), 100))
                    .replace("\n", "\\n"));
        }
        System.out.println("============================================================\n");
    }

    private Path findFirstPdf() throws IOException {
        assertTrue(Files.isDirectory(RESUME_DIR), "简历目录不存在: " + RESUME_DIR);

        Path pdf;
        try (var stream = Files.list(RESUME_DIR)) {
            pdf = stream
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .findFirst()
                    .orElse(null);
        }

        if (pdf == null) {
            throw new IOException("目录中没有 PDF 文件: " + RESUME_DIR);
        }
        return pdf;
    }

    // ==================== 复用 MockMultipartFile ====================

    private static class MockMultipartFile
            implements org.springframework.web.multipart.MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        MockMultipartFile(String name, String originalFilename,
                          String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes; }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            Files.write(dest.toPath(), bytes);
        }
    }
}
