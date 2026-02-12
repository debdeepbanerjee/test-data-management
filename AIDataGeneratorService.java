package com.example.tdm.service.ai;

import com.example.tdm.model.domain.Order;
import com.example.tdm.model.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for generating synthetic test data using Spring AI
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIDataGeneratorService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${tdm.ai.max-generation-size:1000}")
    private int maxGenerationSize;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double defaultTemperature;

    /**
     * Generate type-safe structured data using Spring AI's BeanOutputConverter
     */
    public <T> List<T> generateTypedData(
            Class<T> clazz,
            int count,
            String businessRules,
            Double temperature) {

        if (count > maxGenerationSize) {
            throw new IllegalArgumentException(
                    String.format("Cannot generate more than %d records at once", maxGenerationSize));
        }

        log.info("Generating {} records of type {}", count, clazz.getSimpleName());

        BeanOutputConverter<List<T>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<T>>() {});

        String format = converter.getFormat();

        String promptText = buildPromptForTypedGeneration(clazz, count, businessRules, format);

        ChatClient chatClient = chatClientBuilder
                .defaultOptions(opts -> opts.temperature(temperature != null ? temperature : defaultTemperature))
                .build();

        try {
            List<T> result = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(new ParameterizedTypeReference<List<T>>() {});

            log.info("Successfully generated {} records", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error generating typed data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate data: " + e.getMessage(), e);
        }
    }

    /**
     * Generate data from a JSON schema definition
     */
    public List<Map<String, Object>> generateFromJsonSchema(
            String jsonSchema,
            int count,
            String businessRules,
            Double temperature) {

        log.info("Generating {} records from JSON schema", count);

        String promptText = buildPromptForSchemaGeneration(jsonSchema, count, businessRules);

        ChatClient chatClient = chatClientBuilder
                .defaultOptions(opts -> opts.temperature(temperature != null ? temperature : defaultTemperature))
                .build();

        try {
            List<Map<String, Object>> result = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            log.info("Successfully generated {} records from schema", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error generating data from schema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate data from schema: " + e.getMessage(), e);
        }
    }

    /**
     * Generate data using a custom prompt
     */
    public String generateWithCustomPrompt(String customPrompt, Double temperature) {
        log.info("Generating data with custom prompt");

        ChatClient chatClient = chatClientBuilder
                .defaultOptions(opts -> opts.temperature(temperature != null ? temperature : defaultTemperature))
                .build();

        try {
            String result = chatClient.prompt()
                    .user(customPrompt)
                    .call()
                    .content();

            log.info("Successfully generated data with custom prompt");
            return result;

        } catch (Exception e) {
            log.error("Error generating data with custom prompt: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate data: " + e.getMessage(), e);
        }
    }

    /**
     * Build prompt for type-safe generation
     */
    private String buildPromptForTypedGeneration(
            Class<?> clazz,
            int count,
            String businessRules,
            String format) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate exactly ").append(count)
                .append(" realistic test data records for a ")
                .append(clazz.getSimpleName()).append(" entity.\n\n");

        prompt.append("Requirements:\n");
        prompt.append("- All data must be realistic and believable\n");
        prompt.append("- Ensure diversity in the generated data\n");
        prompt.append("- Follow standard formats for emails, phone numbers, dates, etc.\n");

        if (businessRules != null && !businessRules.isEmpty()) {
            prompt.append("\nAdditional Business Rules:\n").append(businessRules).append("\n");
        }

        prompt.append("\nYour response must conform to this format:\n").append(format);

        return prompt.toString();
    }

    /**
     * Build prompt for schema-based generation
     */
    private String buildPromptForSchemaGeneration(
            String jsonSchema,
            int count,
            String businessRules) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate exactly ").append(count)
                .append(" realistic test data records that conform to the following JSON schema:\n\n");
        prompt.append(jsonSchema).append("\n\n");

        prompt.append("Requirements:\n");
        prompt.append("- All data must be realistic and believable\n");
        prompt.append("- Ensure diversity in the generated data\n");
        prompt.append("- Strictly follow the provided schema structure\n");
        prompt.append("- Use appropriate data types and formats\n");

        if (businessRules != null && !businessRules.isEmpty()) {
            prompt.append("\nAdditional Business Rules:\n").append(businessRules).append("\n");
        }

        prompt.append("\nReturn the data as a JSON array of objects.");

        return prompt.toString();
    }

    /**
     * Specialized method for generating User data
     */
    public List<User> generateUsers(int count, String businessRules, Double temperature) {
        String rules = businessRules != null ? businessRules : 
                "Age should be between 18 and 65. Use realistic US addresses. Email should match the format firstname.lastname@domain.com";
        
        return generateTypedData(User.class, count, rules, temperature);
    }

    /**
     * Specialized method for generating Order data
     */
    public List<Order> generateOrders(int count, String businessRules, Double temperature) {
        String rules = businessRules != null ? businessRules :
                "Generate orders from the past 30 days. Each order should have 1-5 items. Total amount should be the sum of all items. Use realistic product names and prices.";
        
        return generateTypedData(Order.class, count, rules, temperature);
    }
}
