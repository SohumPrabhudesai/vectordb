package com.agentic.demo.service.impl;

import com.agentic.demo.service.ChatService;
import org.apache.tika.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
  private final OllamaChatModel chatModel;

 private final VectorStore vectorStore;

  @Value("classpath:templates/get-answer-template.st")
  private Resource getAnswerTemplate;

  public ChatServiceImpl(OllamaChatModel chatModel, VectorStore vectorStore) {
    this.chatModel = chatModel;
    this.vectorStore = vectorStore;
  }

  @Override
  public String getAnswer(String question,Double temperature){

List<Document> documentList=vectorStore.similaritySearch(SearchRequest.builder().
        topK(2).query(question).build());


      String context = documentList.stream().filter(f->f.getScore()!=null ).filter(e->e.getScore()>0.5)
              .map(Document::getText)
              .collect(Collectors.joining("\n\n"));
    ChatResponse response = chatModel.call(
            new Prompt( StringUtils.isEmpty(context) ?"Here is some context that might be useful to answer the question:\n" + context + "\n\nAnswer the following question based on the above context:\n":"" +
                    question,
                    OllamaChatOptions.builder()
                            .model(OllamaModel.MISTRAL)
                            .temperature(temperature)
                            .build()
            ));
    return response.getResults().toString();
  }

    @Override
    public Boolean loadVectorDb(MultipartFile file) throws IOException {
        List<Document> documents = getDocuments(file);

        vectorStore.add(documents);
        return true;
    }

    private static @NonNull List<Document> getDocuments(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();

        // Wrap into Spring Resource
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Use TikaDocumentReader
        TikaDocumentReader reader = new TikaDocumentReader(resource);

        return reader.read();

    }
}