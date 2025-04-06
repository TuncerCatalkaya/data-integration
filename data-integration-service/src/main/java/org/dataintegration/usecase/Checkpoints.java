package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.service.CheckpointsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.CheckpointsMethods;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;

import java.util.UUID;

/**
 * Checkpoints methods implementation.
 */
@RequiredArgsConstructor
class Checkpoints implements CheckpointsMethods {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;

    @Override
    public CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        return checkpointsService.getCurrentCheckpointStatus(scopeEntity);
    }
}
