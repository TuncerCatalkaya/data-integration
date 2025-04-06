package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateMappingModel {

    private Map<String, Set<String>> duplicatedValues;
    private Map<String, Set<String>> namesNotInHost;
    private Set<String> emptyValues;

}
