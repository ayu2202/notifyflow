package com.notifyflow.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notifyFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NotifyFlow User Service API")
                        .description("Event creation and RabbitMQ publishing service for NotifyFlow")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NotifyFlow")
                                .url("https://github.com/notifyflow")));
    }
}
