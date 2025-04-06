package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.FrontendDotEnvModel;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetEnvironments {

    private final Environment environment;
    private final FrontendDotEnvModel frontendDotEnvModel;

    public Map<String, Object> getFrontend() {
        return frontendDotEnvModel.getKeys().stream()
                .collect(Collectors.toMap(k -> k, environment::getProperty));
    }

}
