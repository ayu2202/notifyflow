package com.notifyflow.gateway.controller;

import com.notifyflow.gateway.entity.DemoEventRequest;
import com.notifyflow.gateway.entity.DemoStatusResponse;
import com.notifyflow.gateway.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {

    private final DemoService demoService;

    public GatewayController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/gateway/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of("status", "UP", "service", "gateway-service")));
    }

    @GetMapping("/demo/status")
    public Mono<ResponseEntity<DemoStatusResponse>> demoStatus() {
        DemoStatusResponse response = new DemoStatusResponse(
                "NotifyFlow",
                "running",
                List.of("gateway-service", "user-service", "notification-service", "rabbitmq")
        );
        return Mono.just(ResponseEntity.ok(response));
    }

    @PostMapping("/demo/event")
    public Mono<ResponseEntity<Map<String, Object>>> demoEvent(@RequestBody DemoEventRequest request) {
        return demoService.sendDemoEvent(request)
                .map(result -> ResponseEntity.ok(result));
    }
}
