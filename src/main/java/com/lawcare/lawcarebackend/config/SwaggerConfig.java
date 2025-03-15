package com.lawcare.lawcarebackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "LawCare API",
        version = "v1",
        description = "외국인 노동자를 위한 AI 기반 권리 보호 서비스 LawCare API 문서",
        contact = @Contact(name = "김은채", email = "ke808762@gmail.com"))
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicAPI() {
        return GroupedOpenApi.builder()
                             .group("lawcare-public")
                             .pathsToMatch("/api/**")
                             .build();
    }
}
