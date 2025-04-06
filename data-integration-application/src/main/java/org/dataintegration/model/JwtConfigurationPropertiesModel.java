package org.dataintegration.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "jwt")
@Configuration
@Data
public class JwtConfigurationPropertiesModel {

    private String authoritiesClaimName;
    private String authorityPrefix;
    private RSAPublicKey accessTokenPub;

}
