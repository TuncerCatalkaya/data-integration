package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DataIntegrationResultAPIModel {

    @NonNull
    private UUID itemId;
    @NonNull
    private List<String> errorMessages;

}
