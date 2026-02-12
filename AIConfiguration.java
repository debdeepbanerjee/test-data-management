package com.example.tdm.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for Spring AI ChatClient
 */
@Configuration
public class AIConfiguration {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    /**
     * ChatClient builder for dependency injection
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * OpenAI Chat Model configuration (active when openai profile is used)
     */
    @Bean
    @Profile("openai")
    public ChatModel openAiChatModel() {
        return new OpenAiChatModel(new OpenAiApi(openAiApiKey));
    }

    // Note: Ollama ChatModel is auto-configured by spring-ai-ollama-spring-boot-starter
    // when the ollama profile is active
}
