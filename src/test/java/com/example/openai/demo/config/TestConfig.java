package com.example.openai.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class TestConfig {

  @Bean
  public TaskExecutor taskExecutor() {
    return new SyncTaskExecutor(); // Runs tasks on the calling thread
  }

}
