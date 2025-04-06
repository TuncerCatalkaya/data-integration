package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationResultAPIModel {

    @NonNull
    private DataIntegrationInputDataAPIModel input;
    @NonNull
    private List<String> errorMessages;

}
