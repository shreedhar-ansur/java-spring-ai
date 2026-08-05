package com.example.openai.demo.service;

import com.example.openai.demo.model.Country;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

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

  public ChatClientService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                           @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                           @Qualifier("ollamaChatClientWithDefaultSystemMessage") ChatClient ollamaChatClientWithDefaultSystemMessage,
                           OpenAIClient client) {
    this.openAiChatClient = openAiChatClient;
    this.ollamaAiChatClient = ollamaChatClient;
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

  public Flux<String> askStream(String message) {
    return ollamaAiChatClient
            .prompt(message)
            .stream()
            .content();
  }

  public List<Country.CountryCities> askForModel(String message) {
    return ollamaAiChatClient
            .prompt()
            .user(message)
            .call()
            .entity(new ParameterizedTypeReference<>() {});
  }

  public String ask(String message) {
    return ollamaAiChatClient
            .prompt(message)
            .call()
            .content();
  }

}
