package com.example.tdm.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sample domain model for generating user test data
 * This demonstrates Spring AI's type-safe structured output
 */
public record User(
        @JsonProperty(required = true)
        String firstName,
        
        @JsonProperty(required = true)
        String lastName,
        
        @JsonProperty(required = true)
        String email,
        
        @JsonProperty(required = true)
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
