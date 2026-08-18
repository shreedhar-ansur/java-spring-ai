package com.example.openai.demo;

import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        OpenAiEmbeddingAutoConfiguration.class
        // Exclude OllamaEmbeddingAutoConfiguration.class instead if using openAi
})
public class DemoAiApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoAiApplication.class, args);
  }

}
