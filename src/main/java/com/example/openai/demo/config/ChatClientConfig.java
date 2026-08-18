package com.example.openai.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
    return MessageWindowChatMemory.builder().maxMessages(5).chatMemoryRepository(jdbcChatMemoryRepository).build();
  }

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
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
  }*/
  @Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
    Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new SimpleLoggerAdvisor(), memoryAdvisor)
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
