package com.example.openai.demo.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ChatClientService {

  private final ChatClient openAiChatClient;
  private final ChatClient ollamaAiChatClient;
  private final ChatClient ollamaAiChatClientWithDefaultSystem;
  private final OpenAIClient client;
  @Value("${openai.model}")
  private String model;

  @Value("classpath:/templates/systemPromptTemplate.st")
  private Resource systemPromptTemplate;

  public ChatClientService(@Qualifier("openAiChatClient") ChatClient chatClientBuilder,
                           @Qualifier("ollamaChatClient") ChatClient ollamaChatClientBuilder,
                           @Qualifier("ollamaChatClientWithDefaultSystemMessage") ChatClient ollamaChatClientWithDefaultSystemMessage,
                           OpenAIClient client) {
    this.openAiChatClient = chatClientBuilder;
    this.ollamaAiChatClient = ollamaChatClientBuilder;
    this.ollamaAiChatClientWithDefaultSystem = ollamaChatClientWithDefaultSystemMessage;
    this.client = client;
  }

  public String askOpenAiClient(String prompt) {
    ChatCompletion completion = client.chat().completions().create(
            ChatCompletionCreateParams.builder()
                    .model(model)
                    .addUserMessage(prompt)
                    .build()
    );

    return completion.choices().get(0).message().content().orElse("Could not get response from LLM");
  }

  public String askOpenAiChatClient(String message) {
    return openAiChatClient
            .prompt(message)
            .call()
            .content();
  }

  public String askOllamaAiChatClient(String message) {
    return ollamaAiChatClient
            .prompt(message)
            .call()
            .content();
  }

  public String askFlightAssistant(String message) {
    return ollamaAiChatClient
            .prompt(message)
            .system("You are flight ticket booking assistant.")
            .call()
            .content();
  }

  public String askAssistantWithDefaultSystem(String message, String additionalSystemMessage) {
    var prompt = ollamaAiChatClientWithDefaultSystem.prompt(message);
    if (additionalSystemMessage != null) {
      prompt = prompt.system(additionalSystemMessage);
    }
    return prompt.call().content();
  }

  public String askAssistantWithSystemPromptTemplate(String message) {
    var prompt = ollamaAiChatClientWithDefaultSystem.prompt(message).system(systemPromptTemplate);
    return prompt.call().content();
  }

  public String ask(String message) {
    return ollamaAiChatClient
            .prompt(message)
            .call()
            .content();
  }

}
