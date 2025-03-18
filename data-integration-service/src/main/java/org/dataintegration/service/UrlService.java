package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.CorsConfigurationPropertiesModel;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final CorsConfigurationPropertiesModel corsConfigurationProperties;

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
