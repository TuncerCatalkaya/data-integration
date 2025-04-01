package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappingNotFoundException;
import org.dataintegration.exception.runtime.MappingValidationException;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappingRepository;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MappingsService {

    private final JpaMappingRepository jpaMappingRepository;

    public MappingEntity createOrUpdateMapping(MappingEntity mappingEntity) {
        return jpaMappingRepository.save(mappingEntity);
    }

    public List<MappingEntity> getAll(UUID scopeId) {
        return jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    public MappingEntity get(UUID mappingId) {
        return jpaMappingRepository.findById(mappingId)
                .orElseThrow(() -> new MappingNotFoundException("Mapping with id " + mappingId + " not found."));
    }

    public void markForDeletion(UUID mappingId) {
        jpaMappingRepository.markForDeletion(mappingId);
    }

    public void markForDeletionByScope(UUID scopeId) {
        final List<MappingEntity> mappingEntities =
                jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.unsorted());
        mappingEntities.forEach(mappingEntity -> mappingEntity.setDelete(true));
        jpaMappingRepository.saveAll(mappingEntities);
    }

    @SuppressWarnings("checkstyle:MethodLength")
    public void validateMapping(UUID mappingId, Map<String, String[]> mapping,
                                List<DataIntegrationHeaderDataAPIModel> dataIntegrationHeaders) {
        final String errorPrefix = "Mapping with id " + mappingId + " ";
        final Set<String> hostTargets = dataIntegrationHeaders.stream()
                .map(DataIntegrationHeaderDataAPIModel::getId)
                .collect(Collectors.toSet());

        final Map<String, String> valueCache = new HashMap<>();
        final Map<String, String> duplicatedValues = new HashMap<>();
        final Map<String, String> namesNotInHost = new HashMap<>();
        final Set<String> emptyValues = new HashSet<>();

        for (Map.Entry<String, String[]> valueEntry : mapping.entrySet()) {
            for (String value : valueEntry.getValue()) {
                final String key = valueEntry.getKey();
                if (!StringUtils.hasText(value)) {
                    emptyValues.add(key);
                } else if (valueCache.containsKey(value)) {
                    final String rootDuplicateKey = valueCache.get(value);
                    duplicatedValues.put(key, value);
                    duplicatedValues.put(rootDuplicateKey, value);
                }
                valueCache.putIfAbsent(value, key);

                if (!hostTargets.contains(value)) {
                    namesNotInHost.put(key, value);
                }
            }
        }

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
