package com.khasanshin.notificationservice.infrastructure.persistence;

import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationInboxEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataNotificationInboxRepository extends JpaRepository<NotificationInboxEntity, UUID>, JpaSpecificationExecutor<NotificationInboxEntity> {

    @Query("""
    select i from NotificationInboxEntity i
    join fetch i.event e
    where i.deletedAt is null
      and i.recipientEmployeeId = :employeeId
      and (:unreadOnly is null or (:unreadOnly = true and i.readAt is null) or (:unreadOnly = false))
      and (:source is null or e.source = :source)
      and (:eventType is null or e.eventType = :eventType)
      and (:from is null or i.deliveredAt >= :from)
      and (:to is null or i.deliveredAt <= :to)
    """)
    Page<NotificationInboxEntity> findInboxByEmployee(
            UUID employeeId,
            Boolean unreadOnly,
            String source,
            String eventType,
            Instant from,
            Instant to,
            Pageable pageable
    );

    long countByRecipientEmployeeIdAndReadAtIsNullAndDeletedAtIsNull(UUID employeeId);

    @Query("""
select count(i) from NotificationInboxEntity i
where i.deletedAt is null
  and i.readAt is null
  and (
    (:me is not null and i.recipientEmployeeId = :me)
    or
    (:rolesPresent = true and i.recipientRole in :roles)
  )
""")
    long countUnreadForUser(@Param("me") UUID me,
                            @Param("roles") Collection<String> roles,
                            @Param("rolesPresent") boolean rolesPresent);

    @Modifying
    @Query("""
    update NotificationInboxEntity i
       set i.readAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.recipientEmployeeId = :me
       and i.readAt is null
    """)
    int markRead(UUID id, UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInboxEntity i
       set i.readAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.readAt is null
    """)
    int markReadAdmin(UUID id, Instant now);

    @Modifying
    @Query("""
    update NotificationInboxEntity i
       set i.readAt = :now
     where i.deletedAt is null
       and i.recipientEmployeeId = :me
       and i.readAt is null
    """)
    int markAllRead(UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInboxEntity i
       set i.deletedAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.recipientEmployeeId = :me
    """)
    int softDelete(UUID id, UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInboxEntity i
       set i.deletedAt = :now
     where i.id = :id
       and i.deletedAt is null
    """)
    int softDeleteAdmin(UUID id, Instant now);

    @Modifying
    @Query("""
      update NotificationInboxEntity i
         set i.readAt = :now
       where i.deletedAt is null
         and i.readAt is null
         and i.recipientEmployeeId = :me
    """)
    int markAllReadByEmployee(@Param("me") UUID me, @Param("now") Instant now);

    @Modifying
    @Query("""
      update NotificationInboxEntity i
         set i.readAt = :now
       where i.deletedAt is null
         and i.readAt is null
         and i.recipientRole in :roles
    """)
    int markAllReadByRoles(@Param("roles") Collection<String> roles, @Param("now") Instant now);
}
