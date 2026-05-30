package com.agentic.demo.service.impl;

import com.agentic.demo.service.ChatService;
import org.springframework.ai.chat.model.ChatModel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {
  private final OllamaChatModel chatModel;

 // private final VectorStore vectorStore;

  @Value("classpath:templates/get-answer-template.st")
  private Resource getAnswerTemplate;

  public ChatServiceImpl(OllamaChatModel chatModel) {
    this.chatModel = chatModel;

  }

  @Override
  public String getAnswer(String question,Double temperature){

    ChatResponse response = chatModel.call(
            new Prompt(
                    question,
                    OllamaChatOptions.builder()
                            .model(OllamaModel.MISTRAL)
                            .temperature(temperature)
                            .build()
            ));
    return response.getResults().toString();
  }
}