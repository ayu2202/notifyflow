package com.notifyflow.user.service;

import com.notifyflow.user.config.RabbitMQConfig;
import com.notifyflow.user.entity.Event;
import com.notifyflow.user.repository.EventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    public EventService(EventRepository eventRepository, RabbitTemplate rabbitTemplate) {
        this.eventRepository = eventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Event createEvent(Event event) {
        Event savedEvent = eventRepository.save(event);
        log.info("Event saved with id: {}", savedEvent.getId());

        publishToRabbitMQ(savedEvent);

        return savedEvent;
    }

    @Retry(name = "rabbitmq-publish", fallbackMethod = "publishFallback")
    @CircuitBreaker(name = "rabbitmq-publish", fallbackMethod = "publishFallback")
    public void publishToRabbitMQ(Event event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Event published to RabbitMQ: {}", event.getEventType());
    }

    private void publishFallback(Event event, Throwable throwable) {
        log.error("Failed to publish event {} to RabbitMQ: {}. Fallback triggered.",
                event.getId(), throwable.getMessage());
    }
}
