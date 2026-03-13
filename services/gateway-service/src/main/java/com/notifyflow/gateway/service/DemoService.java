package com.notifyflow.gateway.service;

import com.notifyflow.gateway.entity.DemoEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final WebClient webClient;

    public DemoService(@Value("${USER_SERVICE_URL:http://localhost:8081}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    public Mono<Map<String, Object>> sendDemoEvent(DemoEventRequest request) {
        Map<String, String> eventPayload = Map.of(
                "eventType", "DEMO_EVENT",
                "userEmail", request.email(),
                "message", request.message()
        );

        log.info("Sending demo event for: {}", request.email());

        return webClient.post()
                .uri("/api/events")
                .bodyValue(eventPayload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("Demo event processed successfully");
                    return Map.of(
                            "status", (Object) "success",
                            "message", "Event sent and notification triggered",
                            "event", response
                    );
                })
                .onErrorResume(error -> {
                    log.error("Demo event failed: {}", error.getMessage());
                    return Mono.just(Map.of(
                            "status", (Object) "error",
                            "message", "Failed to process event: " + error.getMessage()
                    ));
                });
    }
}
