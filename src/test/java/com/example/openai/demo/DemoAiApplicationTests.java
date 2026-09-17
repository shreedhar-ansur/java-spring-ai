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
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
  private FactCheckingEvaluator factCheckingEvaluator;

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

  private static final String DEFAULT_EVALUATION_PROMPT_TEXT_FACT_CHECK = """
				Evaluate whether or not the following claim is supported by the provided document.
				Respond with "yes" if the claim is supported, or "no" if it is not.
				If no document or context is provided use your general knowledge.

				Document:
				{document}

				Claim:
				{claim}
			""";

  @BeforeEach
  void setUp() {
    ChatClient.Builder openAiChatClientBuilder = ChatClient.builder(openAiChatModel);
    this.relevancyEvaluator = RelevancyEvaluator.builder().chatClientBuilder(openAiChatClientBuilder).build();
    this.factCheckingEvaluator = FactCheckingEvaluator.builder(openAiChatClientBuilder).evaluationPrompt(DEFAULT_EVALUATION_PROMPT_TEXT_FACT_CHECK).build();
  }

  @Test
  @DisplayName("Should return relevant responses for basic question with ai response as context")
  @Timeout(30)
  void evaluateChatbotControllerRelevancyWithAiResponseContext() {
    // Given
    String question = "What is capital of india?";

    // --- NO STUBBING FOR EITHER CHAT MODEL ---
    // 1. ChatbotController calls real local Ollama LLM
    // 2. RelevancyEvaluator calls real remote OpenAI LLM

    // When
    // A. Generate live response from Ollama
    String aiResponse = chatbotController.getResponse(question);
    String aiResponseContext = chatbotController.askOpenAi(question);

    // B. Evaluate live response using OpenAI
    EvaluationRequest evaluationRequest = new EvaluationRequest(question, List.of(Document.builder().text(aiResponseContext).build()), aiResponse);
    EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

    // Then
    System.out.println(evaluationResponse.getScore() + " -- ");
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

  @Test
  @DisplayName("Should return relevant responses for basic question")
  void evaluateChatbotControllerRelevancy() {
    // Given
    String question = "What is capital of india?";

    // --- NO STUBBING FOR EITHER CHAT MODEL ---
    // 1. ChatbotController calls real local Ollama LLM
    // 2. RelevancyEvaluator calls real remote OpenAI LLM

    // When
    // A. Generate live response from Ollama
    String aiResponse = chatbotController.getResponse(question);

    // B. Evaluate live response using OpenAI
    EvaluationRequest evaluationRequest = new EvaluationRequest(question, aiResponse);
    EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);

    // Then
    System.out.println(evaluationResponse.getScore() + " -- ");
    Assertions.assertAll(
            () -> assertThat(aiResponse).isNotBlank(),
            () -> assertThat(evaluationResponse.isPass()).withFailMessage("""
                =================================================
                The Ollama response was not considered relevant by OpenAI.
                Question: %s
                Ollama Response: %s
                OpenAI Feedback: %s
                =================================================
                """, question, aiResponse, evaluationResponse.getFeedback()).isTrue()
    );
  }

}
