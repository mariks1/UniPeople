package com.khasanshin.authservice.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "app_user")
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @ToString.Include
    private UUID id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Setter
    private Set<String> roles;

    @Setter
    private UUID employeeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_managed_dept_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "department_id")
    @Setter
    private Set<UUID> managedDeptIds;

    @Builder.Default
    @Setter
    private boolean enabled = true;

    public void changePasswordHash(String hashed) {
        this.passwordHash = hashed;
    }
}
