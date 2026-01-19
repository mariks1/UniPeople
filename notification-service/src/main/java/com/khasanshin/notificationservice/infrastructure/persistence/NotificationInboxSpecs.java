package com.khasanshin.notificationservice.infrastructure.persistence;

import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationInboxEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationInboxSpecs {

    private NotificationInboxSpecs() {}

    public static Specification<NotificationInboxEntity> inboxForUser(
            UUID me,
            Collection<String> roles,
            Boolean unreadOnly,
            String source,
            String eventType,
            Instant from,
            Instant to
    ) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("event", JoinType.INNER);
                query.distinct(true);
            }

            Join<NotificationInboxEntity, NotificationEventEntity> e = root.join("event", JoinType.INNER);

            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.isNull(root.get("deletedAt")));

            boolean hasMe = me != null;
            boolean hasRoles = roles != null && !roles.isEmpty();

            if (!hasMe && !hasRoles) {
                return cb.disjunction();
            }

            var recipientPredicates = cb.disjunction();
            if (hasMe) {
                recipientPredicates = cb.or(recipientPredicates, cb.equal(root.get("recipientEmployeeId"), me));
            }
            if (hasRoles) {
                recipientPredicates = cb.or(recipientPredicates, root.get("recipientRole").in(roles));
            }
            predicates = cb.and(predicates, recipientPredicates);

            if (unreadOnly != null && unreadOnly) {
                predicates = cb.and(predicates, cb.isNull(root.get("readAt")));
            }

            if (source != null && !source.isBlank()) {
                predicates = cb.and(predicates, cb.equal(e.get("source"), source));
            }
            if (eventType != null && !eventType.isBlank()) {
                predicates = cb.and(predicates, cb.equal(e.get("eventType"), eventType));
            }

            if (from != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("deliveredAt"), from));
            }
            if (to != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("deliveredAt"), to));
            }

            return predicates;
        };
    }

    public static Specification<NotificationInboxEntity> inboxByEmployee(
            UUID employeeId,
            Boolean unreadOnly,
            String source,
            String eventType,
            Instant from,
            Instant to
    ) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("event", JoinType.INNER);
                query.distinct(true);
            }
            Join<NotificationInboxEntity, NotificationEventEntity> e = root.join("event", JoinType.INNER);

            var p = cb.conjunction();
            p = cb.and(p, cb.isNull(root.get("deletedAt")));
            p = cb.and(p, cb.equal(root.get("recipientEmployeeId"), employeeId));

            if (unreadOnly != null && unreadOnly) {
                p = cb.and(p, cb.isNull(root.get("readAt")));
            }
            if (source != null && !source.isBlank()) {
                p = cb.and(p, cb.equal(e.get("source"), source));
            }
            if (eventType != null && !eventType.isBlank()) {
                p = cb.and(p, cb.equal(e.get("eventType"), eventType));
            }
            if (from != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("deliveredAt"), from));
            }
            if (to != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("deliveredAt"), to));
            }

            return p;
        };
    }
}
