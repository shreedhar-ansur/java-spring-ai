package com.example.openai.demo.controller;

import com.example.openai.demo.model.Country;
import com.example.openai.demo.service.ChatClientService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

  @GetMapping("/bean")
  public ResponseEntity<List<Country.CountryCities>> getResponseBean(@RequestParam String message) {
    return ResponseEntity.ok(chatClientService.askForModel(message));
  }

  @PostMapping
  public String postChatMessage(@RequestBody String message) {
    return "Message received: " + message;
  }
}
