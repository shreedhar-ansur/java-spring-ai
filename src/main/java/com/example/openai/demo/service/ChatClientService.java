package com.example.openai.demo.service;

import com.example.openai.demo.model.Country;
import com.example.openai.demo.tools.HelpDeskTools;
import com.example.openai.demo.tools.TimeTools;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ChatClientService {

  private final ChatClient openAiChatClient;
  private final ChatClient ollamaAiChatClient;
  private final ChatClient ollamaAiChatClientWithDefaultSystem;
  private final ChatClient ollamahelpDeskChatClient;
  private final OpenAIClient client;
  private final VectorStore vectorStore;
  @Value("${openai.model}")
  private String model;

  @Value("classpath:/templates/systemPromptTemplate.st")
  private Resource systemPromptTemplate;

  /*@Value("classpath:/templates/systemPromptTemplateRag.st")
  private Resource systemPromptTemplateRag;*/

  @Value("classpath:/templates/systemPromptTemplateHelpDesk.st")
  private Resource systemPromptTemplateHelpDesk;

  private final HelpDeskTools helpDeskTools;

  public ChatClientService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                           @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                           @Qualifier("ollamahelpDeskChatClient") ChatClient ollamahelpDeskChatClient,
                           @Qualifier("ollamaChatClientWithDefaultSystemMessage") ChatClient ollamaChatClientWithDefaultSystemMessage,
                           OpenAIClient client, VectorStore vectorStore, HelpDeskTools helpDeskTools) {
    this.openAiChatClient = openAiChatClient;
    this.ollamaAiChatClient = ollamaChatClient;
    this.ollamahelpDeskChatClient = ollamahelpDeskChatClient;
    this.ollamaAiChatClientWithDefaultSystem = ollamaChatClientWithDefaultSystemMessage;
    this.client = client;
    this.vectorStore = vectorStore;
    this.helpDeskTools = helpDeskTools;
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

  public String askUsingRag(String message, String userId) {
    /*List<Document> similarDocs = vectorStore.similaritySearch(SearchRequest.builder().query(message).similarityThreshold(0.5).topK(3).build());
    String similarContext = similarDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining(System.lineSeparator()));*/
    return ollamaAiChatClient
            .prompt()
            //.system(promptSystemSpec -> promptSystemSpec.text(systemPromptTemplateRag).param("documents", similarContext))
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
            .user(message)
            .call()
            .content();
  }

  public String askUsingTools(String message, String userId) {
    return ollamaAiChatClient
            .prompt()
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
            .user(message)
            .call()
            .content();
  }

  public String askUsingHelpDeskTools(String message, String username) {
    return ollamahelpDeskChatClient
            .prompt()
            .system(systemPromptTemplateHelpDesk)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
            .user(message)
            .tools(helpDeskTools)
            .toolContext(Map.of("username", username))
            .call()
            .content();
  }

  public String ask(String message) {
    return ollamaAiChatClient
            .prompt()
            .user(message)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, "default"))
            .call()
            .content();
  }

}
