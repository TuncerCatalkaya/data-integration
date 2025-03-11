package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class DataIntegrationAPIModel {

    @NonNull
    private List<DataIntegrationResultAPIModel> integrationResults;

}
