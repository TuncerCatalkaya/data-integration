package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationAPIModel {

    private List<Map<String, DataIntegrationResultAPIModel>> integrationResults;

}
