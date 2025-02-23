package org.dataintegration.api;

import org.dataintegration.model.DataIntegrationEndpointsAPIModel;

@FunctionalInterface
public interface DataIntegrationEndpointsAPI {
    DataIntegrationEndpointsAPIModel listEndpoints();
}
