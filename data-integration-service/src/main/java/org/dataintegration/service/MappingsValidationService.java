package org.dataintegration.service;

import org.dataintegration.exception.runtime.MappingValidationException;
import org.dataintegration.model.ValidateMappingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service for mapping validation logic.
 */
@Service
public class MappingsValidationService {

    /**
     * Logic for validating a mapping.
     * It detects currently: duplicated values, names that are not in host, empty values
     *
     * @param mapping mapping {@link Map} key {@link String} = source, value {@link String}[] = targets
     * @param hostTargets available host target header names
     * @return {@link ValidateMappingModel}
     */
    public ValidateMappingModel validateMapping(Map<String, String[]> mapping, Set<String> hostTargets) {
        final Map<String, Set<String>> targetToSources = new HashMap<>();
        final Map<String, Set<String>> duplicatedValues = new HashMap<>();
        final Map<String, Set<String>> namesNotInHost = new HashMap<>();
        final Set<String> emptyValues = new HashSet<>();

        for (Map.Entry<String, String[]> valueEntry : mapping.entrySet()) {
            for (String target : valueEntry.getValue()) {
                final String source = valueEntry.getKey();
                if (!StringUtils.hasText(target)) {
                    emptyValues.add(source);
                    continue;
                }

                targetToSources.computeIfAbsent(target, k -> new HashSet<>()).add(source);

                if (!hostTargets.contains(target)) {
                    final Set<String> namesNotInHostList = namesNotInHost.getOrDefault(source, new HashSet<>());
                    namesNotInHostList.add(target);
                    namesNotInHost.put(source, namesNotInHostList);
                }
            }
        }

        for (Map.Entry<String, Set<String>> entry : targetToSources.entrySet()) {
            String target = entry.getKey();
            Set<String> sources = entry.getValue();

            if (sources.size() > 1) {
                for (String source : sources) {
                    duplicatedValues.computeIfAbsent(source, k -> new HashSet<>()).add(target);
                }
            }
        }

        return ValidateMappingModel.builder()
                .duplicatedValues(duplicatedValues)
                .namesNotInHost(namesNotInHost)
                .emptyValues(emptyValues)
                .build();
    }

    /**
     * Error handler for mapping validation.
     *
     * @param errorPrefix error prefix (text)
     * @param validateMapping {@link ValidateMappingModel}
     * @throws MappingValidationException in case there is a validation error present
     */
    public void validateMappingErrorHandler(String errorPrefix, ValidateMappingModel validateMapping) {
        final Map<String, Set<String>> duplicatedValues = validateMapping.getDuplicatedValues();
        final Map<String, Set<String>> namesNotInHost = validateMapping.getNamesNotInHost();
        final Set<String> emptyValues = validateMapping.getEmptyValues();

        String targetErrorMsg = errorPrefix + "has one or more target errors.";
        if (!duplicatedValues.isEmpty()) {
            targetErrorMsg += " Duplicated values are present in: " + duplicatedValues + ".";
        }
        if (!emptyValues.isEmpty()) {
            targetErrorMsg += " Empty values are present in following targets: " + emptyValues + ".";
        }
        if (!namesNotInHost.isEmpty()) {
            targetErrorMsg += " Following targets have a name that does not exist on the host: " + namesNotInHost + ".";
        }
        if (!duplicatedValues.isEmpty() || !emptyValues.isEmpty() || !namesNotInHost.isEmpty()) {
            throw new MappingValidationException(targetErrorMsg);
        }
    }

}
