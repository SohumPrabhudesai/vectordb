package com.agentic.demo.controller;

import com.agentic.demo.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    @GetMapping("/ask/questions")
    public String beginChat(@RequestParam(value = "question") String question,@RequestParam(value = "temperature")Double temperature) {
        return chatService.getAnswer(question,temperature);
    }
    @PostMapping(path="/load/vectordb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Boolean beginChat(@RequestParam("file") MultipartFile file) throws IOException {
        return chatService.loadVectorDb(file);
    }
}
