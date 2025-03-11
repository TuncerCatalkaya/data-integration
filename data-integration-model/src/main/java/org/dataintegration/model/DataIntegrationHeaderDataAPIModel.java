package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class DataIntegrationHeaderDataAPIModel {

    @NonNull
    private String id;
    @NonNull
    private String display;
    @NonNull
    private String tooltip;

}
