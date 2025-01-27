package org.dataintegration.config.swagger;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

/**
 * Swagger OperationCustomizer. Adding automatically in description what permission is needed for the endpoint
 * (through {@link PreAuthorize} annotation). If no permission is required (no {@link PreAuthorize} annotation present), then
 * the description will be "This api is public".
 */
public class SwaggerOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        final Optional<PreAuthorize> preAuthorizeAnnotation =
                Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class));
        final StringBuilder sb = new StringBuilder();
        if (preAuthorizeAnnotation.isPresent()) {
            sb.append("This api requires **")
                    .append((preAuthorizeAnnotation.get()).value())
                    .append("** permission.");
        } else {
            sb.append("This api is **public**");
        }
        sb.append("<br /><br />");
        sb.append(operation.getDescription() != null ? operation.getDescription() : "");
        operation.setDescription(sb.toString());
        return operation;
    }
}
