package com.notifyflow.gateway.entity;

public record DemoEventRequest(
        String email,
        String message
) {
}
