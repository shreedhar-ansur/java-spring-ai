package com.example.openai.demo.controller;

import com.example.openai.demo.service.ChatClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

  private final ChatClientService chatClientService;

  public ChatbotController(ChatClientService chatClientService) {
    this.chatClientService = chatClientService;
  }

  @GetMapping
  public String getResponse(@RequestParam String message) {
    return chatClientService.ask(message);
  }

  @PostMapping
  public String postChatMessage(@RequestBody String message) {
    return "Message received: " + message;
  }
}
