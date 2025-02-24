package org.dataintegration.api;

import org.dataintegration.model.DataIntegrationEndpointsAPIModel;

public interface DataIntegrationEndpointsAPI {
    default DataIntegrationEndpointsAPIModel listEndpoints(String getHeadersPath, String integrationPath) {
        return DataIntegrationEndpointsAPIModel.builder()
                .getHeadersPath(getHeadersPath)
                .integrationPath(integrationPath)
                .build();
    }
}
