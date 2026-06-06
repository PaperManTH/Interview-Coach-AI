package com.interviewcoach.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 简历文件解析工具 — PDF / DOCX / TXT → 纯文本。
 */
@Slf4j
public final class ResumeParserUtils {

    private ResumeParserUtils() {}

    public static String parse(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null) throw new IOException("文件名为空");

        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return parsePdf(file.getInputStream());
        if (lower.endsWith(".docx")) return parseDocx(file.getInputStream());
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".rtf")) {
            return new String(file.getBytes(), "UTF-8");
        }
        throw new IOException("不支持的文件格式: " + name);
    }

    private static String parsePdf(InputStream in) throws IOException {
        try (PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            log.info("[ResumeParser] PDF 解析完成，字符数: {}", text.length());
            return text;
        }
    }

    private static String parseDocx(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            log.info("[ResumeParser] DOCX 解析完成，字符数: {}", text.length());
            return text;
        }
    }
}
