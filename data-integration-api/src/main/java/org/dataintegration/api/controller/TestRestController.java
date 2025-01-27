package org.dataintegration.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "/test")
@RestController
@RequestMapping("data-integration-api/test")
@RequiredArgsConstructor
public class TestRestController {

    @GetMapping
    public String test() {
        return "test";
    }

}
