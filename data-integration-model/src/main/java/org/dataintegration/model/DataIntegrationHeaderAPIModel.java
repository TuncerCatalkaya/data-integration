package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class DataIntegrationHeaderAPIModel {

    @NonNull
    private List<DataIntegrationHeaderDataAPIModel> headers;

}
