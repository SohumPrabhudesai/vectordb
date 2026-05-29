package com.agentic.demo.dto;

import lombok.Data;

import java.util.List;
@Data
public class SearchRequest {
    private String queryText;
    private int topK;

    // getters and setters
}
