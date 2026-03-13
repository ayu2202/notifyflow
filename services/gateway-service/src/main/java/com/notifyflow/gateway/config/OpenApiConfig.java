package com.notifyflow.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notifyFlowGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NotifyFlow Gateway API")
                        .description("API Gateway for NotifyFlow microservices system. "
                                + "Routes requests to downstream services and provides demo endpoints.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NotifyFlow")
                                .url("https://github.com/notifyflow")));
    }
}
