package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostModel {

    private UUID id;
    private String name;
    private String baseUrl;
    private String integrationPath;
    private String getHeadersPath;
    private boolean inUse;

    @Builder.Default
    private List<DatabaseModel> databases = new ArrayList<>();

}
