package com.khasanshin.notificationservice.mapper;

import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import com.khasanshin.notificationservice.domain.model.NotificationInbox;
import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.dto.NotificationEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = NotificationViewMapper.class)
public interface NotificationMapper {

    @Mapping(target = "inboxId", source = "id")
    @Mapping(target = "unread", expression = "java(inbox.getReadAt() == null)")
    InboxItemDto toInboxDto(NotificationInbox inbox);

    @Mapping(target = "title", source = "event", qualifiedByName = "title")
    NotificationEventDto toEventDto(NotificationEvent event);
}
