package com.notifyflow.notification.service;

import com.notifyflow.notification.entity.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = "events.queue")
    public void handleEvent(EventMessage event) {
        log.info("Notification sent to {}: {}", event.getUserEmail(), event.getMessage());
    }
}
