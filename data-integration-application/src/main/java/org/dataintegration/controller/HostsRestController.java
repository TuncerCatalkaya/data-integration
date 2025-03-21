package org.dataintegration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.DataIntegrationHeaderAPIModel;
import org.dataintegration.model.HostModel;
import org.dataintegration.usecase.HostsUsecase;
import org.dataintegration.usecase.model.CreateOrUpdateHostsRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@Tag(name = "/hosts")
@RestController
@RequestMapping("${server.root.path}/hosts")
@RequiredArgsConstructor
public class HostsRestController {

    private final HostsUsecase hostsUsecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping
    public HostModel createOrUpdateHost(@RequestBody @Valid CreateOrUpdateHostsRequestModel createOrUpdateHostsRequest) {
        return hostsUsecase.createOrUpdateHost(createOrUpdateHostsRequest);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Set<HostModel> getHosts() {
        return hostsUsecase.getAllHosts();
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{hostId}/headers")
    public DataIntegrationHeaderAPIModel getHostHeaders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostId, @RequestParam String language) {
        return hostsUsecase.getHostHeaders(hostId, language, jwt.getTokenValue());
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{hostId}")
    public void deleteHost(@PathVariable UUID hostId) {
        hostsUsecase.deleteHost(hostId);
    }

}
