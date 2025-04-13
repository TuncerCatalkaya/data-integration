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
public class DataIntegrationHeaderDataAPIModel {

    @NonNull
    private String id;
    @NonNull
    private String display;
    private boolean optional;
    @NonNull
    private String tooltip;
    @NonNull
    private List<String> alternatives;

}
