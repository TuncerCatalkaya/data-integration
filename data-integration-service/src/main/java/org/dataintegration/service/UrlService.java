package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.CorsConfigurationPropertiesModel;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for URLs.
 */
@Service
@RequiredArgsConstructor
public class UrlService {

    private final CorsConfigurationPropertiesModel corsConfigurationProperties;

    /**
     * Checks if a domain is valid by checking if domain is part of the cors configuration domains.
     *
     * @param url url that should be checked
     * @return true if valid, false if not valid
     */
    public boolean isDomainValid(String url) {
        return corsConfigurationProperties.getAllowedOriginPatterns().stream()
                .anyMatch(allowedOriginPattern -> {
                    if (allowedOriginPattern.contains("*")) {
                        final String regex = convertGlobToRegex(allowedOriginPattern);
                        final Pattern pattern = Pattern.compile(regex);
                        return pattern.matcher(url).matches();
                    } else {
                        return url.startsWith(allowedOriginPattern);
                    }
                });
    }

    private String convertGlobToRegex(String globPattern) {
        return "^" + globPattern.replace("*", ".*") + "$";
    }

}
