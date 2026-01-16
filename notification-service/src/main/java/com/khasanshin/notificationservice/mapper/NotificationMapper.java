package com.khasanshin.notificationservice.mapper;

import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.dto.NotificationEventDto;
import com.khasanshin.notificationservice.entity.NotificationEvent;
import com.khasanshin.notificationservice.entity.NotificationInbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final NotificationViewMapper titles;

    public InboxItemDto toInboxDto(NotificationInbox i) {
        var e = i.getEvent();
        return new InboxItemDto(
                i.getId(),
                i.getReadAt() == null,
                i.getDeliveredAt(),
                toEventDto(e)
        );
    }

    public NotificationEventDto toEventDto(NotificationEvent e) {
        return new NotificationEventDto(
                e.getEventId(),
                e.getCreatedAt(),
                e.getSource(),
                e.getEventType(),
                e.getEntityId(),
                titles.title(e),
                e.getPayload()
        );
    }
}
