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
  public String getAnswer(String question){

    ChatResponse response = chatModel.call(
            new Prompt(
                    "Generate the names of 5 famous pirates.",
                    OllamaChatOptions.builder()
                            .model(OllamaModel.MISTRAL)
                            .temperature(0.4)
                            .build()
            ));
   /* List<Document> documentList =
            vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());
    List<String> contentList = documentList.stream().map(Document::getFormattedContent).toList();

    BeanOutputConverter<String> gameWeekBeanOutputConverter =
            new BeanOutputConverter<>(String.class);
    String format = gameWeekBeanOutputConverter.getFormat();
    PromptTemplate promptTemplate = new PromptTemplate("");
    Prompt prompt =
            promptTemplate.create(
                    Map.of(
                            "gameweekNumber",
                            question,
                            "format",
                            format,
                            "documents",
                            String.join("\n", contentList)));
    // return chatClient.prompt(prompt).user(question).stream().content();
    ChatResponse chatResponse = chatModel.call(prompt);*/
    //String output = Objects.requireNonNull(chatResponse.getResult().getOutput().getText());
    return response.getResults().toString();
  }
}