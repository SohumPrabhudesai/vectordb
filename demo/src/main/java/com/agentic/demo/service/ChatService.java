package com.agentic.demo.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatService {

   String getAnswer(String question,Double temperature);
   Boolean loadVectorDb(MultipartFile file) throws IOException;
}