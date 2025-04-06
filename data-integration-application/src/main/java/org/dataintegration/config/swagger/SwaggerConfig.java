package org.dataintegration.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger documentation configuration. Activated Bearer authentication for passing jwt token.
 */
@Configuration
public class SwaggerConfig {

    @Value("${swagger.server.url}")
    private String serverUrl;

    @Bean
    OpenAPI openAPI(BuildProperties buildProperties) {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Data integration application - REST API Documentation")
                        .description(
                                "### Maven properties"
                                        + "\n\n" + "**group**: " + "*" + buildProperties.getGroup() + "*"
                                        + "\n\n" + "**name**: " + "*" + buildProperties.getName() + "*"
                                        + "\n\n" + "**artifact**: " + "*" + buildProperties.getArtifact() + "*"
                                        + "\n\n" + "**version**: " + "*" + buildProperties.getVersion() + "*"
                                        + "\n\n" + "**timestamp**: " + "*" + buildProperties.getTime().toString() + "*"
                        )
                        .version(buildProperties.getVersion())
                )
                .servers(List.of(
                                new Server()
                                        .description("Server")
                                        .url(serverUrl)
                        )
                );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .description(
                        "Enter your JWT access token")
                .scheme("bearer")
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT");
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return new SwaggerOperationCustomizer();
    }
}
