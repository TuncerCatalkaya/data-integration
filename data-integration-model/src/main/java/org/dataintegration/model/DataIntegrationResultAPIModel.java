package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationResultAPIModel {

    private DataIntegrationStateAPIModel dataIntegrationState;
    private List<String> messages;

}
