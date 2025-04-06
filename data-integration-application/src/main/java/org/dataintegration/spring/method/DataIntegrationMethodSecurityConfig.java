package org.dataintegration.spring.method;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Config to create bean of {@link MethodSecurityExpressionHandler}.
 */
@Configuration
@EnableMethodSecurity
public class DataIntegrationMethodSecurityConfig {

    @Bean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DataIntegrationMethodSecurityExpressionHandler();
    }
}
