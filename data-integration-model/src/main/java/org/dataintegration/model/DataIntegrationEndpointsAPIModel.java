package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationEndpointsAPIModel {

    private String validateInputsPath;
    private String getHeadersPath;
    private String integrationPath;

}
