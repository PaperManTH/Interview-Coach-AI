package com.interviewcoach.service.impl;

import com.interviewcoach.entity.ResumeProfile;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.exception.FileUploadException;
import com.interviewcoach.mapper.ResumeProfileMapper;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.utils.ResumeParserUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ResumeServiceImpl 单元测试 — 覆盖校验 / 解析 / LLM / 入库主流程。
 */
class ResumeServiceImplTest {

    private ResumeProfileMapper mapper;
    private SpringAiService aiService;
    private ResumeServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(ResumeProfileMapper.class);
        aiService = mock(SpringAiService.class);
        service = new ResumeServiceImpl(
                mapper,
                aiService,
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }

    // ==================== 格式校验 ====================

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf",
                "application/pdf", new byte[0]);

        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> service.uploadAndParse(file, "user-1"));
        assertEquals("EMPTY_FILE", ex.getCode());
    }

    @Test
    void shouldRejectUnsupportedFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", "fake".getBytes());

        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> service.uploadAndParse(file, "user-1"));
        assertEquals("UNSUPPORTED_FORMAT", ex.getCode());
    }

    @Test
    void shouldRejectOversizedFile() {
        byte[] big = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf",
                "application/pdf", big);

        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> service.uploadAndParse(file, "user-1"));
        assertEquals("FILE_TOO_LARGE", ex.getCode());
    }

    // ==================== TXT 正常流程（不需要真实 LLM） ====================

    @Test
    void shouldParseTxtAndStore(@TempDir Path tmpDir) throws IOException {
        String content = "John Doe\nSkills: Java, Spring\nExperience: 5 years at Google";
        Path txtFile = tmpDir.resolve("resume.txt");
        Files.writeString(txtFile, content);

        MockMultipartFile file = new MockMultipartFile("file", "resume.txt",
                "text/plain", Files.readAllBytes(txtFile));

        // mock LLM 返回合法 JSON
        when(aiService.chatWithSystem(anyString(), anyString()))
                .thenReturn("{\"candidateSummary\":\"Experienced engineer\",\"skills\":[\"Java\"],\"projects\":[],\"workExperience\":[],\"strengths\":[],\"possibleQuestions\":[]}");

        doAnswer(inv -> {
            ResumeProfile p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        }).when(mapper).insert(any(ResumeProfile.class));

        ResumeResponse resp = service.uploadAndParse(file, "user-1");

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("resume.txt", resp.getFileName());
        assertNotNull(resp.getParsedData());
        assertEquals("Experienced engineer", resp.getParsedData().getCandidateSummary());
        assertTrue(resp.getParsedData().getSkills().contains("Java"));

        // 验证入库调用
        ArgumentCaptor<ResumeProfile> captor = ArgumentCaptor.forClass(ResumeProfile.class);
        verify(mapper).insert(captor.capture());
        assertEquals("user-1", captor.getValue().getUserId());
        assertTrue(captor.getValue().getResumeText().contains("John Doe"));
    }

    // ==================== LLM 返回非 JSON 兜底 ====================

    @Test
    void shouldFallbackWhenLLMReturnsInvalidJson(@TempDir Path tmpDir) throws IOException {
        String content = "Simple resume";
        Path txtFile = tmpDir.resolve("simple.txt");
        Files.writeString(txtFile, content);

        MockMultipartFile file = new MockMultipartFile("file", "simple.txt",
                "text/plain", Files.readAllBytes(txtFile));

        when(aiService.chatWithSystem(anyString(), anyString()))
                .thenReturn("Sorry, I cannot process this.");

        doAnswer(inv -> {
            ResumeProfile p = inv.getArgument(0);
            p.setId(2L);
            return 1;
        }).when(mapper).insert(any(ResumeProfile.class));

        ResumeResponse resp = service.uploadAndParse(file, "user-2");

        assertNotNull(resp);
        assertEquals(2L, resp.getId());

        // parsedData 应为 null（因为入库的是 "{}"）
        assertNull(resp.getParsedData());

        ArgumentCaptor<ResumeProfile> captor = ArgumentCaptor.forClass(ResumeProfile.class);
        verify(mapper).insert(captor.capture());
        assertEquals("{}", captor.getValue().getParsedJson());
    }

    // ==================== 文件解析工具测试 ====================

    @Test
    void shouldParseTxtFile(@TempDir Path tmpDir) throws IOException {
        String content = "Hello Resume World";
        Path txtFile = tmpDir.resolve("test.txt");
        Files.writeString(txtFile, content);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", Files.readAllBytes(txtFile));

        String parsed = ResumeParserUtils.parse(file);
        assertEquals(content, parsed);
    }

    // ==================== 简单 MockMultipartFile ====================

    /** 轻量 MultipartFile 实现，不依赖 Spring MockMvc。 */
    private static class MockMultipartFile implements org.springframework.web.multipart.MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        MockMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
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

        @Override public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(bytes);
        }
        @Override public void transferTo(java.io.File dest) throws IOException {
            Files.write(dest.toPath(), bytes);
        }
    }
}
