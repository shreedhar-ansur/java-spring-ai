package com.example.openai.demo.controller;

import com.example.openai.demo.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openai/chat")
public class OpenAiController {

  private final AiService aiService;

  OpenAiController(AiService aiService) {
    this.aiService = aiService;
  }

  @GetMapping
  public String getChatResponse(@RequestParam String message) {
    return aiService.ask(message);
  }

}
