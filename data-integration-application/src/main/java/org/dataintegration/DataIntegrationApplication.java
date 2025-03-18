package org.dataintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Data integration application launcher.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@EnableJpaRepositories
@EnableJpaAuditing
@EnableConfigurationProperties
@SpringBootApplication
@lombok.Generated // skip coverage
public class DataIntegrationApplication {

    /**
     * Traditional main method. Launches spring boot application.
     *
     * @param args start arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DataIntegrationApplication.class, args);
    }
}