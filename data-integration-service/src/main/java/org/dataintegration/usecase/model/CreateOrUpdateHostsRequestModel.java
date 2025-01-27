package org.dataintegration.usecase.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class CreateOrUpdateHostsRequestModel {

    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String url;

    @Valid
    @Builder.Default
    private List<CreateOrUpdateDatabasesRequestModel> databases = new ArrayList<>();

}
