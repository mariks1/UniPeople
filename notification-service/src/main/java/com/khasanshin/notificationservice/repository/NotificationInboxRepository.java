package com.khasanshin.notificationservice.repository;

import com.khasanshin.notificationservice.entity.NotificationInbox;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface NotificationInboxRepository extends JpaRepository<NotificationInbox, UUID>, JpaSpecificationExecutor<NotificationInbox> {


    @Query("""
    select i from NotificationInbox i
    join fetch i.event e
    where i.deletedAt is null
      and i.recipientEmployeeId = :employeeId
      and (:unreadOnly is null or (:unreadOnly = true and i.readAt is null) or (:unreadOnly = false))
      and (:source is null or e.source = :source)
      and (:eventType is null or e.eventType = :eventType)
      and (:from is null or i.deliveredAt >= :from)
      and (:to is null or i.deliveredAt <= :to)
    """)
    Page<NotificationInbox> findInboxByEmployee(
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
select count(i) from NotificationInbox i
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
    update NotificationInbox i
       set i.readAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.recipientEmployeeId = :me
       and i.readAt is null
    """)
    int markRead(UUID id, UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInbox i
       set i.readAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.readAt is null
    """)
    int markReadAdmin(UUID id, Instant now);

    @Modifying
    @Query("""
    update NotificationInbox i
       set i.readAt = :now
     where i.deletedAt is null
       and i.recipientEmployeeId = :me
       and i.readAt is null
    """)
    int markAllRead(UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInbox i
       set i.deletedAt = :now
     where i.id = :id
       and i.deletedAt is null
       and i.recipientEmployeeId = :me
    """)
    int softDelete(UUID id, UUID me, Instant now);

    @Modifying
    @Query("""
    update NotificationInbox i
       set i.deletedAt = :now
     where i.id = :id
       and i.deletedAt is null
    """)
    int softDeleteAdmin(UUID id, Instant now);

    @Modifying
    @Query("""
      update NotificationInbox i
         set i.readAt = :now
       where i.deletedAt is null
         and i.readAt is null
         and i.recipientEmployeeId = :me
    """)
    int markAllReadByEmployee(@Param("me") UUID me, @Param("now") Instant now);

    @Modifying
    @Query("""
      update NotificationInbox i
         set i.readAt = :now
       where i.deletedAt is null
         and i.readAt is null
         and i.recipientRole in :roles
    """)
    int markAllReadByRoles(@Param("roles") Collection<String> roles, @Param("now") Instant now);

}
