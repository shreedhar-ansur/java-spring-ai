package com.example.openai.demo.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HRPolicyLoader {

  VectorStore vectorStore;

  @Value("classpath:HR_Policies.pdf")
  private Resource hrPolicyDocument;

  public HRPolicyLoader(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  @PostConstruct
  public void loadPdf() {
    TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPolicyDocument);
    List<Document> documentList = tikaDocumentReader.get();
    TextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(200).withMaxNumChunks(400).build();
    vectorStore.add(textSplitter.split(documentList));
  }

}
