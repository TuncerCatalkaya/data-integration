package org.dataintegration.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.dataintegration.model.FrontendDotEnvModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DotEnvConfiguration {

    private static final String DIRECTORY = "./data-integration-ui/";

    @Bean
    FrontendDotEnvModel frontendDotEnvModel(ConfigurableEnvironment configurableEnvironment) {
        final Dotenv dotenv = Dotenv.configure()
                .directory(DIRECTORY)
                .load();
        final Map<String, Object> dotenvMap = dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE).stream()
                .collect(Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue));
        final MapPropertySource propertySource = new MapPropertySource("dotenvProperties", dotenvMap);
        configurableEnvironment.getPropertySources().addLast(propertySource);
        return FrontendDotEnvModel.builder()
                .keys(dotenvMap.keySet())
                .build();
    }

}
