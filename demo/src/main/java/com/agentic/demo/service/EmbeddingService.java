package com.agentic.demo.service;

import java.util.List;

public interface EmbeddingService {
    List<Float> embedText(String text) throws Exception;
}