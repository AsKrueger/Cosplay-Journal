package com.cosplayjournal.infrastructure.adapter.out.security;

import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.domain.exception.ForbiddenAccessException;
import com.cosplayjournal.domain.model.user.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public Optional<UserId> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String principalName = auth.getName();
        if (principalName == null || principalName.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(UserId.of(principalName));
    }

    @Override
    public UserId getRequiredCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new ForbiddenAccessException("Se requiere un usuario autenticado para realizar esta operación"));
    }

    @Override
    public boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }
}
