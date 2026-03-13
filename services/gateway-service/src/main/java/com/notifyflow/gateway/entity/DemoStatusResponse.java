package com.notifyflow.gateway.entity;

import java.util.List;

public record DemoStatusResponse(
        String system,
        String status,
        List<String> services
) {
}
