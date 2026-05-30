package com.agentic.demo.controller;

import com.agentic.demo.dto.SearchRequest;
import com.agentic.demo.dto.SearchResponse;
import com.agentic.demo.service.EmbeddingService;
import com.agentic.demo.service.MilvusService;
import com.agentic.demo.util.PdfChunker;
import com.agentic.demo.util.TextExtractor;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.String;
@RestController
@RequestMapping("/vector")
public class VectorDBController {

    private final EmbeddingService embeddingService;
    private final MilvusService milvusService;
    private final PdfChunker pdfChunker;
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public VectorDBController(EmbeddingService embeddingService,
                              MilvusService milvusService,
                              PdfChunker pdfChunker) {
        this.embeddingService = embeddingService;
        this.milvusService = milvusService;
        this.pdfChunker = pdfChunker;
    }

    @PostMapping(path = "/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "collection", required = false, defaultValue = "documents") String collection) {
        try {
            // 1) Save file locally (optional)
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : UUID.randomUUID().toString();
            Path out = uploadPath.resolve(filename);
            Files.write(out, file.getBytes());
            String storedPath = out.toAbsolutePath().toString();

            List<String> chunks = pdfChunker.chunkPdf(file, 1000); // 1000 chars per chunk

            for (String chunk : chunks) {
                List<Float> vector = embeddingService.embedText(chunk);
                // 4) Insert into Milvus (store embedding + metadata)
                String docId = milvusService.insertVector(collection, vector, Map.of(
                        "filename", filename,
                        "path", storedPath,
                        "content", chunk));
            }
            return ResponseEntity.ok(Map.of("collection", collection));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{collectionName}")
    public ResponseEntity<?> search(
            @PathVariable String collectionName,
            @RequestBody SearchRequest request) throws Exception {


        //create embeddings from text
        List<Float> queryVector = embeddingService.embedText(request.getQueryText());
        List<SearchResponse> content = new ArrayList<>();
        R<SearchResults> resultData = milvusService.searchDocuments(collectionName, queryVector, request.getTopK());

        if (resultData.getStatus() != R.Status.Success.getCode()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Search failed: " + resultData.getStatus());
        }
        SearchResultData searchResult = resultData.getData().getResults();
        SearchResultsWrapper wrapper = new SearchResultsWrapper(resultData.getData().getResults());

// Loop through each row in the search results
        List<Float> records = searchResult.getScoresList();

        for (int i = 0; i < records.size(); i++) {
            float score =records.get(i);

            // Align with scalar field data
            Object contentField = wrapper.getFieldWrapper("content").getFieldData().get(i);

            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setMatchedText(contentField.toString());
            searchResponse.setMatchedScore(score);

            content.add(searchResponse);

            System.out.println("Result " + (i + 1));
            System.out.println("Score: " + score);
            System.out.println("Content: " + contentField);
            System.out.println("-------------------");
        }
 return ResponseEntity.ok(content);
    }
}