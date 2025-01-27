package org.dataintegration.usecase.api;

import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;

import java.util.UUID;

public interface CheckpointsMethods {
    CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String createdBy);
}
