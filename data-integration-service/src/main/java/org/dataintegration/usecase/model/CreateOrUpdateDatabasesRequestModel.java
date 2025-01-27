package org.dataintegration.usecase.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateDatabasesRequestModel {

    private UUID id;

    @NotBlank
    private String name;

}
