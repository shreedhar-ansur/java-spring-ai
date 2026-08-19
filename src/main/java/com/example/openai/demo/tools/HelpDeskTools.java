package com.example.openai.demo.tools;

import com.example.openai.demo.entity.HelpDeskTicket;
import com.example.openai.demo.model.TicketRequest;
import com.example.openai.demo.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

  private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskTools.class);

  private final HelpDeskTicketService helpDeskTicketService;

  @Tool(name = "createTicket", description = "Create the support ticket", returnDirect = true)
  String CreateTicket(@ToolParam(description = "Details to create a support ticket")TicketRequest ticketRequest, ToolContext toolContext) {
    String username = (String) toolContext.getContext().get("username");
    HelpDeskTicket helpDeskTicket = helpDeskTicketService.createTicket(ticketRequest, username);
    return "ticket #" + helpDeskTicket.getId() + " created successfully for username " + helpDeskTicket.getUsername();
  }

  @Tool(name = "getTicket", description = "fetch the status of the tickets based on a given username")
  List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {
    return helpDeskTicketService.getTicketsByUsername((String) toolContext.getContext().get("username"));
  }
}
