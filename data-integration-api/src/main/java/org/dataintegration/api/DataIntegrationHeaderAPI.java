package org.dataintegration.api;

import org.dataintegration.model.DataIntegrationHeaderAPIModel;

@FunctionalInterface
public interface DataIntegrationHeaderAPI {
    DataIntegrationHeaderAPIModel getHeaders(String language);
}
