package com.lawcare.lawcarebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LawcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(LawcareApplication.class, args);
    }

}
