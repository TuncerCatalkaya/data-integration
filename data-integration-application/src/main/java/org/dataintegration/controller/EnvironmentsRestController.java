package org.dataintegration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dataintegration.usecase.GetEnvironments;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "/environments")
@RestController
@RequestMapping("${server.root.path}/environments")
@RequiredArgsConstructor
public class EnvironmentsRestController {

    private final GetEnvironments getEnvironments;

    @GetMapping("/frontend")
    public Map<String, Object> getFrontendEnvironments() {
        return getEnvironments.getFrontend();
    }

}
