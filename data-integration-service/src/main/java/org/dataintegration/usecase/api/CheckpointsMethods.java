package org.dataintegration.usecase.api;

import org.dataintegration.usecase.ProjectsUsecase;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;

import java.util.UUID;

/**
 * Checkpoints methods for {@link ProjectsUsecase}.
 */
public interface CheckpointsMethods {

    /**
     * Get current checkpoint status.
     *
     * @param projectId project id
     * @param scopeId scope id
     * @param createdBy creator
     * @return {@link CurrentCheckpointStatusResponseModel}
     */
    CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String createdBy);
}
