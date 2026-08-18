package com.bookstore.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Book Store - Order Service API")
                        .version("1.0.0")
                        .description("Member 2 order management microservice"))
                .components(new Components().addSecuritySchemes("ApiKeyAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-KEY")));
    }
}

