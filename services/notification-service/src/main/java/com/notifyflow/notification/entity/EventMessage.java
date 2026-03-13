package com.notifyflow.notification.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EventMessage implements Serializable {

    private Long id;
    private String eventType;
    private String userEmail;
    private String message;
    private LocalDateTime createdAt;

    public EventMessage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
