package org.dataintegration.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.DataIntegrationHeaderAPIModel;
import org.dataintegration.model.DataIntegrationInputAPIModel;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class DataIntegrationControllerAPI {

    private final DataIntegrationHeaderAPI dataIntegrationHeaderAPI;
    private final DataIntegrationAPI dataIntegrationAPI;

    public abstract DataIntegrationHeaderAPIModel getHeadersRestCall(String language);

    protected DataIntegrationHeaderAPIModel defaultGetHeadersRestCall(String language) {
        return dataIntegrationHeaderAPI.getHeaders(language);
    }

    public abstract DataIntegrationAPIModel integrationRestCall(String database, String language,
                                                                DataIntegrationInputAPIModel dataIntegrationInput);

    protected DataIntegrationAPIModel defaultIntegrationRestCall(String database, String language,
                                                                 DataIntegrationInputAPIModel dataIntegrationInput) {
        return dataIntegrationAPI.doIntegration(database, language, dataIntegrationInput);
    }

}
