package com.teachengineering.springai.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class FirstAssistantController {

    private final ChatClient chatClient;

    public FirstAssistantController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String getAssistant(@RequestParam String message) {
        return chatClient.prompt()
                         .user(message)
                         .call()
                         .content();
    }
}
