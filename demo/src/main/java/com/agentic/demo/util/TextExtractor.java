package com.agentic.demo.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TextExtractor {

    public String extractText(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String name = file.getOriginalFilename();
        String text = "";

        if (name != null && name.toLowerCase().endsWith(".pdf")) {
            try (InputStream is = file.getInputStream(); PDDocument doc = PDDocument.load(is)) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(doc);

                if (text == null || text.trim().isEmpty()) {
                    throw new IllegalArgumentException("PDF is image-based or contains no extractable text. Please use an OCR tool or a searchable PDF.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract text from PDF: " + e.getMessage(), e);
            }
        } else {
            // treat as plain text
            text = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (text.trim().isEmpty()) {
                throw new IllegalArgumentException("Text file is empty");
            }
        }

        return text;
    }
}