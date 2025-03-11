package org.dataintegration.api;

import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.DataIntegrationInputAPIModel;

@FunctionalInterface
public interface DataIntegrationAPI {
    DataIntegrationAPIModel doIntegration(String database, String language, DataIntegrationInputAPIModel dataIntegrationInput);
}
