package org.dataintegration.config;

import lombok.NonNull;
import org.dataintegration.utils.DataIntegrationUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    public @NonNull Optional<String> getCurrentAuditor() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof UsernamePasswordAuthenticationToken
                || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        final Jwt jwt = (Jwt) authentication.getPrincipal();
        return Optional.of(DataIntegrationUtils.getJwtUserId(jwt));
    }
}