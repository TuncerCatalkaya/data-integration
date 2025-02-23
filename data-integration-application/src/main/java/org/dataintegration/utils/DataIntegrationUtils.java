package org.dataintegration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dataintegration.exception.InvalidDelimiterException;
import org.dataintegration.exception.InvalidUUIDException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataIntegrationUtils {

    public static String getJwtUserId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaims().get("userId"))
                .map(String::valueOf)
                .or(() -> Optional.ofNullable(jwt.getSubject()))
                .orElse(null);
    }

    public static UUID getProjectIdFromS3Key(String key) {
        try {
            return UUID.fromString(key.split("/")[0]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUUIDException("Provided key " + key + " does not have a valid UUID as base.");
        }
    }

    public static String getScopeKeyFromS3Key(String key) {
        return key.split("/")[1];
    }

    public static char delimiterStringToCharMapper(String delimiter) {
        return switch (delimiter) {
            case "," -> ',';
            case ";" -> ';';
            case "\\t" -> '\t';
            case "|" -> '|';
            case " " -> ' ';
            default -> throw new InvalidDelimiterException("Delimiter " + delimiter + " is invalid or not supported.");
        };

    }
}
