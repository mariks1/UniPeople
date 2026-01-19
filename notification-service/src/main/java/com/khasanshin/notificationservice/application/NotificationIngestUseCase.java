package com.khasanshin.notificationservice.application;

import com.khasanshin.notificationservice.domain.model.NotificationMessage;

public interface NotificationIngestUseCase {
    void handle(NotificationMessage message);
}
