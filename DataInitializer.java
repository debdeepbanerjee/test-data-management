package com.example.tdm.config;

import com.example.tdm.model.dto.SchemaDefinitionRequest;
import com.example.tdm.model.entity.DataSchema;
import com.example.tdm.service.TestDataManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initialize default schemas on application startup
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final TestDataManagementService tdmService;

    @Bean
    public CommandLineRunner initializeSchemas() {
        return args -> {
            log.info("Initializing default data schemas...");

            // User Schema
            registerUserSchema();

            // Order Schema
            registerOrderSchema();

            log.info("Default schemas initialized successfully");
        };
    }

    private void registerUserSchema() {
        try {
            String userClassDefinition = """
                public record User(
                    String firstName,
                    String lastName,
                    String email,
                    Integer age,
                    String phoneNumber,
                    Address address,
                    String occupation,
                    String company
                ) {
                    public record Address(
                        String street,
                        String city,
                        String state,
                        String zipCode,
                        String country
                    ) {}
                }
                """;

            String businessRules = """
                - Age must be between 18 and 65
                - Email format: firstname.lastname@domain.com
                - Phone numbers should be in US format
                - Use realistic US addresses
                - Company names should be from Fortune 500 or well-known startups
                - Occupations should match the company industry
                """;

            SchemaDefinitionRequest request = SchemaDefinitionRequest.builder()
                    .schemaName("User")
                    .description("User entity with personal and contact information")
                    .schemaDefinition(userClassDefinition)
                    .schemaType(DataSchema.SchemaType.JAVA_CLASS)
                    .businessRules(businessRules)
                    .active(true)
                    .createdBy("system")
                    .build();

            tdmService.registerSchema(request);
            log.info("User schema registered");

        } catch (IllegalArgumentException e) {
            log.info("User schema already exists, skipping...");
        }
    }

    private void registerOrderSchema() {
        try {
            String orderClassDefinition = """
                public record Order(
                    String orderId,
                    String customerId,
                    LocalDateTime orderDate,
                    OrderStatus status,
                    List<OrderItem> items,
                    BigDecimal totalAmount,
                    String shippingAddress,
                    String paymentMethod
                ) {
                    public record OrderItem(
                        String productId,
                        String productName,
                        Integer quantity,
                        BigDecimal unitPrice,
                        BigDecimal totalPrice
                    ) {}
                    
                    public enum OrderStatus {
                        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
                    }
                }
                """;

            String businessRules = """
                - Order IDs should be in format: ORD-YYYYMMDD-XXXX
                - Order dates should be within the past 30 days
                - Each order should have 1-5 items
                - Product names should be realistic e-commerce items
                - Total amount must equal sum of all item totals
                - Unit prices should be reasonable (between $5 and $500)
                - Use realistic payment methods: Credit Card, PayPal, Apple Pay, etc.
                """;

            SchemaDefinitionRequest request = SchemaDefinitionRequest.builder()
                    .schemaName("Order")
                    .description("E-commerce order with line items")
                    .schemaDefinition(orderClassDefinition)
                    .schemaType(DataSchema.SchemaType.JAVA_CLASS)
                    .businessRules(businessRules)
                    .active(true)
                    .createdBy("system")
                    .build();

            tdmService.registerSchema(request);
            log.info("Order schema registered");

        } catch (IllegalArgumentException e) {
            log.info("Order schema already exists, skipping...");
        }
    }
}
