package com.example.openai.demo.config;

import com.example.openai.demo.advisor.TokenUsageAuditAdvisor;
import com.example.openai.demo.rag.PIIMaskingDocumentPostProcessor;
import com.example.openai.demo.rag.WebSearchDocumentRetriever;
import com.example.openai.demo.tools.TimeTools;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ChatClientConfig {

  @Bean
  public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
    return MessageWindowChatMemory.builder().maxMessages(20).chatMemoryRepository(jdbcChatMemoryRepository).build();
  }

  @Bean
  public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore, @Qualifier("ollamaChatClientBuilder") ChatClient.Builder chatClientBuilder) {
    return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(TranslationQueryTransformer.builder().chatClientBuilder(chatClientBuilder.clone()).targetLanguage("english").build())
            .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.5).build())
            .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
            .build();
  }

  @Bean
  public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
    return ChatClient
            .builder(openAiChatModel)
            .build();
  }

  @Bean("ollamaChatClientBuilder")
  ChatClient.Builder ollamaChatClientBuilder(
          @Qualifier("ollamaChatModel") ChatModel chatModel) {

    return ChatClient.builder(chatModel);
  }

  @Bean("openAiChatClientBuilder")
  ChatClient.Builder openAiChatClientBuilder(
          @Qualifier("openAiChatModel") ChatModel chatModel) {

    return ChatClient.builder(chatModel);
  }

  /*@Bean
  ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
    return new DefaultToolExecutionExceptionProcessor(true);
  }*/

  /*@Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
  }*/
  /*@Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor, RestClient.Builder restClientBuilder, SemanticCacheAdvisor semanticCacheAdvisor) {
    Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    //var webSearchDocumentRetreiver = RetrievalAugmentationAdvisor.builder().documentRetriever(WebSearchDocumentRetriever.builder().restClientBuilder(restClientBuilder).maxResults(5).build()).build();
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            //.defaultAdvisors(new TokenUsageAuditAdvisor(), new SimpleLoggerAdvisor(), memoryAdvisor, retrievalAugmentationAdvisor)
            //.defaultAdvisors(new TokenUsageAuditAdvisor(), new SimpleLoggerAdvisor(), memoryAdvisor, webSearchDocumentRetreiver)
            .defaultAdvisors(new TokenUsageAuditAdvisor(), new SimpleLoggerAdvisor(), memoryAdvisor, retrievalAugmentationAdvisor, semanticCacheAdvisor)
            .build();
  }*/

  @Bean
  public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, TimeTools timeTools) {
    Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new TokenUsageAuditAdvisor(), new SimpleLoggerAdvisor(), memoryAdvisor)
            .defaultTools(timeTools)
            .build();
  }

  @Bean
  public ChatClient ollamahelpDeskChatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, TimeTools timeTools) {
    Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    return ChatClient
            .builder(ollamaChatModel)
            .defaultOptions(OllamaChatOptions.builder())
            .defaultAdvisors(new TokenUsageAuditAdvisor(), new SimpleLoggerAdvisor(), memoryAdvisor)
            .defaultTools(timeTools)
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
