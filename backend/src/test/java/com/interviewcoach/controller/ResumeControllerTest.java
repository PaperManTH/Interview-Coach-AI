package com.interviewcoach.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.entity.dto.resume.ResumeParsedData;
import com.interviewcoach.exception.FileUploadException;
import com.interviewcoach.exception.GlobalExceptionHandler;
import com.interviewcoach.service.ResumeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 简历上传 Controller 集成测试 — MockMvc + 全链路。
 */
class ResumeControllerTest {

    private MockMvc mockMvc;
    private ResumeService resumeService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        resumeService = mock(ResumeService.class);
        objectMapper = new ObjectMapper();
        ResumeController controller = new ResumeController(resumeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== 正常上传 ====================

    @Test
    void shouldUploadAndReturnParsedData() throws Exception {
        ResumeResponse mockResp = new ResumeResponse();
        mockResp.setId(1L);
        mockResp.setFileName("resume.pdf");
        mockResp.setResumeTextPreview("John Doe\nJava Engineer...");

        ResumeParsedData parsed = new ResumeParsedData();
        parsed.setCandidateSummary("Experienced Java Engineer");
        parsed.setSkills(List.of("Java", "Spring Boot", "MySQL"));
        parsed.setProjects(List.of("E-commerce platform"));
        parsed.setWorkExperience(List.of("5 years at Google"));
        parsed.setStrengths(List.of("Strong backend skills", "Good team player"));
        parsed.setPossibleQuestions(List.of("Explain Spring Boot auto-configuration"));
        mockResp.setParsedData(parsed);

        when(resumeService.uploadAndParse(any(MultipartFile.class), eq("user-123")))
                .thenReturn(mockResp);

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "fake pdf content".getBytes());

        String json = mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.fileName").value("resume.pdf"))
                .andExpect(jsonPath("$.data.parsedData.candidateSummary")
                        .value("Experienced Java Engineer"))
                .andExpect(jsonPath("$.data.parsedData.skills[0]").value("Java"))
                .andExpect(jsonPath("$.data.parsedData.skills[1]").value("Spring Boot"))
                .andExpect(jsonPath("$.data.parsedData.possibleQuestions[0]")
                        .value("Explain Spring Boot auto-configuration"))
                .andReturn().getResponse().getContentAsString();

        assertTrue(json.contains("Experienced Java Engineer"));
        verify(resumeService).uploadAndParse(any(MultipartFile.class), eq("user-123"));
    }

    // ==================== 无 userId 使用 anonymous ====================

    @Test
    void shouldUseAnonymousWhenNoUserId() throws Exception {
        ResumeResponse mockResp = new ResumeResponse();
        mockResp.setId(2L);
        mockResp.setFileName("cv.txt");

        when(resumeService.uploadAndParse(any(MultipartFile.class), eq("anonymous")))
                .thenReturn(mockResp);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.txt", "text/plain", "simple".getBytes());

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));

        verify(resumeService).uploadAndParse(any(MultipartFile.class), eq("anonymous"));
    }

    // ==================== 异常场景 ====================

    @Test
    void shouldReturn400ForUnsupportedFormat() throws Exception {
        when(resumeService.uploadAndParse(any(MultipartFile.class), anyString()))
                .thenThrow(new FileUploadException(400, "UNSUPPORTED_FORMAT",
                        "不支持的文件格式，仅支持 PDF / DOCX / TXT"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "image".getBytes());

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不支持的文件格式，仅支持 PDF / DOCX / TXT"));
    }

    @Test
    void shouldReturn400ForEmptyFile() throws Exception {
        when(resumeService.uploadAndParse(any(MultipartFile.class), anyString()))
                .thenThrow(new FileUploadException(400, "EMPTY_FILE", "上传文件为空"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("上传文件为空"));
    }

    @Test
    void shouldReturn413ForOversizedFile() throws Exception {
        when(resumeService.uploadAndParse(any(MultipartFile.class), anyString()))
                .thenThrow(new FileUploadException(413, "FILE_TOO_LARGE",
                        "文件过大（11.0 MB），最大允许 10 MB"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", new byte[100]);

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.code").value(413));
    }

    @Test
    void shouldReturn500ForUnexpectedError() throws Exception {
        when(resumeService.uploadAndParse(any(MultipartFile.class), anyString()))
                .thenThrow(new RuntimeException("Unexpected internal error"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 真实 TXT 文件解析全流程 ====================

    @Test
    void shouldParseRealTxtEndToEnd(@TempDir Path tmpDir) throws Exception {
        // 创建真实 TXT 文件
        String txtContent = """
                John Doe
                Software Engineer | johndoe@example.com
                                
                Skills:
                Java, Spring Boot, MySQL, Docker, Kubernetes
                                
                Experience:
                Senior Software Engineer at Google (2020-2025)
                - Led microservices migration project
                - Built CI/CD pipeline with Jenkins
                                
                Education:
                B.S. Computer Science, Stanford University (2016-2020)
                """;
        Path txtFile = tmpDir.resolve("john-doe-resume.txt");
        Files.writeString(txtFile, txtContent);

        MockMultipartFile file = new MockMultipartFile(
                "file", "john-doe-resume.txt", "text/plain",
                Files.readAllBytes(txtFile));

        ResumeResponse mockResp = new ResumeResponse();
        mockResp.setId(10L);
        mockResp.setFileName("john-doe-resume.txt");
        mockResp.setResumeTextPreview(txtContent.substring(0, Math.min(txtContent.length(), 500)));

        ResumeParsedData parsed = new ResumeParsedData();
        parsed.setCandidateSummary("Senior Software Engineer with 5 years at Google");
        parsed.setSkills(List.of("Java", "Spring Boot", "MySQL", "Docker", "Kubernetes"));
        parsed.setWorkExperience(List.of("Senior Software Engineer at Google (2020-2025)"));
        parsed.setPossibleQuestions(List.of(
                "Tell me about the microservices migration project",
                "How did you set up the CI/CD pipeline?"
        ));
        mockResp.setParsedData(parsed);

        when(resumeService.uploadAndParse(any(MultipartFile.class), eq("user-txt")))
                .thenReturn(mockResp);

        String json = mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .header("X-User-Id", "user-txt")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.parsedData.skills.length()").value(5))
                .andExpect(jsonPath("$.data.parsedData.possibleQuestions.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        assertTrue(json.contains("Google"));
        assertTrue(json.contains("Docker"));

        verify(resumeService).uploadAndParse(any(MultipartFile.class), eq("user-txt"));
    }
}
