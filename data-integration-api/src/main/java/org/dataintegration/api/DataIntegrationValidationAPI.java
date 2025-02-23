package org.dataintegration.api;

import org.dataintegration.model.DataIntegrationInputAPIModel;
import org.dataintegration.model.DataIntegrationValidationAPIModel;

@FunctionalInterface
public interface DataIntegrationValidationAPI {
    DataIntegrationValidationAPIModel validateInputs(DataIntegrationInputAPIModel dataIntegrationInput);
}
