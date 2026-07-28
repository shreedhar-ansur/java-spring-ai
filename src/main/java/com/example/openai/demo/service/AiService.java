package com.example.openai.demo.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

  private final OpenAIClient client;
  @Value("${openai.model}")
  private String model;

  public AiService(OpenAIClient client) {
    this.client = client;
  }

  public String ask(String prompt) {
    ChatCompletion completion = client.chat().completions().create(
            ChatCompletionCreateParams.builder()
                    .model(model)
                    .addUserMessage(prompt)
                    .build()
    );

    return completion.choices().get(0).message().content().orElse("");
  }
}