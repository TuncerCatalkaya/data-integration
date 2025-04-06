package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedHostModel {

    private UUID id;
    private String name;
    private String baseUrl;
    private String integrationPath;
    private String headerPath;

}
