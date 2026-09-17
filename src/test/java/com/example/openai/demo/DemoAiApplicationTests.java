package com.example.openai.demo;

import com.example.openai.demo.controller.ChatbotController;
import com.example.openai.demo.tools.HelpDeskTools;
import com.example.openai.demo.tools.TimeTools;
import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class DemoAiApplicationTests {
  @Autowired
  private ChatbotController chatbotController;

  private final float minScore = 0.7f;

  private RelevancyEvaluator relevancyEvaluator;

  @Autowired
  @Qualifier("ollamaChatModel")
  private OllamaChatModel ollamaChatModel;

  // 2. Inject REAL OpenAI model (auto-configured by Spring AI)
  @Autowired
  @Qualifier("openAiChatModel")
  private OpenAiChatModel openAiChatModel;

  @MockitoBean
  private JdbcChatMemoryRepository jdbcChatMemoryRepository;

  @MockitoBean
  private VectorStore vectorStore;

  @MockitoBean
  private OpenAIClient openAIClient;

  @MockitoBean
  private HelpDeskTools helpDeskTools;

  @MockitoBean
  private TimeTools timeTools;

  @BeforeEach
  void setUp() {
    ChatClient.Builder openAiChatClientBuilder = ChatClient.builder(openAiChatModel);
    this.relevancyEvaluator = new RelevancyEvaluator(openAiChatClientBuilder);
  }

  /*@Test
  @DisplayName("Should process message through ChatClientService and return AI response")
  void evaluateChatbotControllerGetResponse() {
    // Given
    String userQuestion = "what is capital of india?";
    String expectedAnswer = "The capital of India is New Delhi.";

    // Stub the response when ChatModel.call is invoked
    ChatResponse mockChatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(expectedAnswer))));
    given(ollamaChatModel.call(any(Prompt.class))).willReturn(mockChatResponse);

    // When
    String actualResponse = chatbotController.getResponse(userQuestion);

    // Then
    assertThat(actualResponse).isNotBlank();
    assertThat(actualResponse).isEqualTo(expectedAnswer);
  }*/

  /*@Test
  @DisplayName("Should return relevant responses for basic question")
  @Timeout(30)
  void evaluateChatbotControllerRelevancy() {
    // Given
    String question = "what is capital of india?";

    // When
    String aiResponse = chatbotController.getResponse(question);
    EvaluationRequest evaluationRequest = new EvaluationRequest(question, aiResponse);
    EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

    // Then
    Assertions.assertAll(() -> assertThat(aiResponse).isNotBlank(),
            () -> assertThat(evaluationResponse.isPass()).withFailMessage("""
            =================================================
            The answer was not considered relevant.
            Question: %s
            Response: %s
            =================================================
            """, question, aiResponse).isTrue(),
            () -> assertThat(evaluationResponse.getScore()).withFailMessage("""
            =================================================
            The score %.2f is lower than minimum required %.2f.
            Question: %s
            Response: %s
            =================================================
            """, evaluationResponse.getScore(), minScore, question, aiResponse).isGreaterThan(minScore));

  }*/

  @Test
  @DisplayName("Should return relevant responses for basic question")
  void evaluateChatbotControllerRelevancy() {
    // Given
    String question = "what is capital of india?";

    // --- NO STUBBING FOR EITHER CHAT MODEL ---
    // 1. ChatbotController calls real local Ollama LLM
    // 2. RelevancyEvaluator calls real remote OpenAI LLM

    // When
    // A. Generate live response from Ollama
    String aiResponse = chatbotController.getResponse(question);
    //String aiResponseContext = chatbotController.askOpenAiForTesting(question);

    // B. Evaluate live response using OpenAI
    //EvaluationRequest evaluationRequest = new EvaluationRequest(question, List.of(Document.builder().text(aiResponseContext).build()), aiResponse);
    EvaluationRequest evaluationRequest = new EvaluationRequest(question, aiResponse);
    EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

    // Then
    Assertions.assertAll(
            () -> assertThat(aiResponse).isNotBlank(),
            () -> assertThat(evaluationResponse.isPass()).withFailMessage("""
                =================================================
                The Ollama response was not considered relevant by OpenAI.
                Question: %s
                Ollama Response: %s
                OpenAI Feedback: %s
                =================================================
                """, question, aiResponse, evaluationResponse.getFeedback()).isTrue(),
            () -> assertThat((double) evaluationResponse.getScore()).withFailMessage("""
                =================================================
                The score %.2f is lower than minimum required %.2f.
                Question: %s
                Ollama Response: %s
                OpenAI Feedback: %s
                =================================================
                """, evaluationResponse.getScore(), minScore, question, aiResponse, evaluationResponse.getFeedback()).isGreaterThan(minScore)
    );
  }

}
