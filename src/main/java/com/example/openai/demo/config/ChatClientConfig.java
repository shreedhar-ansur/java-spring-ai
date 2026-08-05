package com.example.openai.demo.config;

import com.example.openai.demo.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
    return ChatClient
            .builder(openAiChatModel)
            .build();
  }

  /*@Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
    return ChatClient
            .builder(ollamaChatModel)
            .build();
  }*/
  @Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
  }

  @Bean
  public ChatClient ollamaChatClientWithDefaultSystemMessage(OllamaChatModel ollamaChatModel) {
    return ChatClient
            .builder(ollamaChatModel)
            .defaultSystem("You are a helpful Math AI assistant. Your name is sir curious.")
            .build();
  }
}
