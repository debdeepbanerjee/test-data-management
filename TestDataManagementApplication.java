package com.example.tdm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TestDataManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDataManagementApplication.class, args);
    }
}
