package com.agentic.demo.controller;

import com.agentic.demo.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
