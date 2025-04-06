package org.dataintegration.usecase;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.dataintegration.service.CheckpointsService;
import org.dataintegration.service.HostsService;
import org.dataintegration.service.ItemsService;
import org.dataintegration.service.MappedItemsService;
import org.dataintegration.service.MappingsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.CheckpointsMethods;
import org.dataintegration.usecase.api.ItemsMethods;
import org.dataintegration.usecase.api.MappedItemsMethods;
import org.dataintegration.usecase.api.MappingsMethods;
import org.dataintegration.usecase.api.ProjectsMethods;
import org.dataintegration.usecase.api.ScopesMethods;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectsUsecase {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final CheckpointsService checkpointsService;
    private final MappingsService mappingsService;
    private final MappedItemsService mappedItemsService;
    private final HostsService hostsService;
    private final DataIntegrationCache dataIntegrationCache;
    private final HostsUsecase hostsUsecase;

    @Getter(lazy = true)
    private final ProjectsMethods projectsMethods = new Projects(projectsService, scopesService, mappingsService);

    @Getter(lazy = true)
    private final ScopesMethods scopesMethods = new Scopes(projectsService, scopesService, mappingsService, dataIntegrationCache);

    @Getter(lazy = true)
    private final ItemsMethods itemsMethods = new Items(projectsService, scopesService, itemsService, mappedItemsService);

    @Getter(lazy = true)
    private final CheckpointsMethods checkpointsMethods = new Checkpoints(projectsService, scopesService, checkpointsService);

    @Getter(lazy = true)
    private final MappingsMethods mappingsMethods =
            new Mappings(projectsService, scopesService, itemsService, mappingsService, mappedItemsService, hostsService,
                    hostsUsecase);

    @Getter(lazy = true)
    private final MappedItemsMethods mappedItemsMethods =
            new MappedItems(projectsService, mappingsService, mappedItemsService, hostsUsecase);
}
