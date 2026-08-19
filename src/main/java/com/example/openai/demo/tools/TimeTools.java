package com.example.openai.demo.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeTools {

  private static final Logger logger = LoggerFactory.getLogger(TimeTools.class);

  @Tool(name = "currentLocalTimeRetriever", description = "Get current time in user local timezone")
  String getCurrentLocalTime() {
    return LocalDateTime.now().toString();
  }

  @Tool(name = "currentTimeRetriever", description = "Get current time inspecified timezone")
  String getCurrentTime(@ToolParam(description = "value representing timezone") String timeZone) {
    return LocalDateTime.now(ZoneId.of(timeZone)).toString();
  }

}
