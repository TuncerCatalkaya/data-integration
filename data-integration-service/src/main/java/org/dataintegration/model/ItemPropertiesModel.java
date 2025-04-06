package org.dataintegration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemPropertiesModel {

    private String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String originalValue;

}
