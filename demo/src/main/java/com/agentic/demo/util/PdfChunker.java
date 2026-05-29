package com.agentic.demo.util;

import org.apache.james.mime4j.dom.Multipart;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.*;
@Service
public class PdfChunker {
    public static List<String> chunkPdf(MultipartFile pdfFile, int chunkSize) throws Exception {
        Tika tika = new Tika();
        String text = tika.parseToString( pdfFile.getInputStream());

        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            sb.append(word).append(" ");
            if (sb.length() >= chunkSize) {
                chunks.add(sb.toString().trim());
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            chunks.add(sb.toString().trim());
        }
        return chunks;
    }
}
