package org.dataintegration.usecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateMappingsRequestModel {

    private UUID mappingId;
    private UUID databaseId;
    private String mappingName;
    private Map<String, String[]> mapping;

}
