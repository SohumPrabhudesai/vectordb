package com.agentic.demo.dto;

import lombok.Data;

@Data
public class SearchResponse {

    private Object matchedText;
    private Float matchedScore;
}
