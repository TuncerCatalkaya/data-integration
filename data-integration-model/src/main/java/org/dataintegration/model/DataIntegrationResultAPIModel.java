package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationResultAPIModel {

    private DataIntegrationStateAPIModel dataIntegrationState;
    private String message;

}
