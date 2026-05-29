package com.agentic.demo.service;


import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    private final OkHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ollama.host:http://localhost}")
    private String ollamaHost;

    @Value("${ollama.port:11434}")
    private int ollamaPort;

    @Value("${ollama.model:llama2}")
    private String model;

    public OpenAiEmbeddingService() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<Float> embedText(String text) throws Exception {
        String embeddingsUrl = ollamaHost + ":" + ollamaPort + "/api/embeddings";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        String bodyJson = mapper.writeValueAsString(Map.of(
                "model", model,
                "prompt", text
        ));

        Request request = new Request.Builder()
                .url(embeddingsUrl)
                .post(RequestBody.create(bodyJson, JSON))
                .build();


        try (Response response = http.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            JSONArray arr = json.getJSONArray("embedding");


            List<Float> vector = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                vector.add((float) arr.getDouble(i));
            }
            return vector;
        }
    }
    }
