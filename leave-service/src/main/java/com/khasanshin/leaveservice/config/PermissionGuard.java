package com.khasanshin.leaveservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component("perm")
@Slf4j
public class PermissionGuard {

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return false;
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    private Set<UUID> managed(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return Set.of();
        var raw = jwt.getClaimAsStringList("managedDeptIds");
        if (raw == null) return Set.of();
        Set<UUID> out = new HashSet<>();
        for (String s : raw) try { out.add(UUID.fromString(s)); } catch (Exception ignored) {}
        return out;
    }

    private UUID employeeId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return null;
        String v = jwt.getClaimAsString("employeeId");
        try { return v == null ? null : UUID.fromString(v); } catch (Exception e) { return null; }
    }

    public boolean canManageDept(Authentication auth, UUID deptId) {
        return hasRole(auth, "ORG_ADMIN") || hasRole(auth, "HR") || managed(auth).contains(deptId);
    }

    public boolean isSelf(Authentication auth, UUID empId) {
        var me = employeeId(auth);
        return me != null && me.equals(empId);
    }

    public boolean hasAny(Authentication auth, String... roles) {
        for (String r : roles) if (hasRole(auth, r)) return true;
        return false;
    }
}
