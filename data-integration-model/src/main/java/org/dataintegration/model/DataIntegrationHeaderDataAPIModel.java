package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataIntegrationHeaderDataAPIModel {

    @NonNull
    private String id;
    @NonNull
    private String display;
    @NonNull
    private String tooltip;

}
