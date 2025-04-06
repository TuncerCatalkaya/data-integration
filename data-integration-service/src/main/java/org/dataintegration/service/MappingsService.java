package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappingNotFoundException;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappingRepository;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.dataintegration.model.ValidateMappingModel;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for mappings.
 */
@Service
@RequiredArgsConstructor
public class MappingsService {

    private final JpaMappingRepository jpaMappingRepository;
    private final MappingsValidationService mappingsValidationService;

    /**
     * Create or update mapping.
     *
     * @param mappingEntity {@link MappingEntity}
     * @return {@link MappingEntity}
     */
    public MappingEntity createOrUpdateMapping(MappingEntity mappingEntity) {
        return jpaMappingRepository.save(mappingEntity);
    }

    /**
     * Get all mappings by scope id.
     *
     * @param scopeId scope id
     * @return {@link List} of {@link MappingEntity}
     */
    public List<MappingEntity> getAll(UUID scopeId) {
        return jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    /**
     * Get mapping by mapping id.
     *
     * @param mappingId mapping id
     * @return {@link MappingEntity}
     * @throws MappingNotFoundException in case mapping entity is not found in database
     */
    public MappingEntity get(UUID mappingId) {
        return jpaMappingRepository.findById(mappingId)
                .orElseThrow(() -> new MappingNotFoundException("Mapping with id " + mappingId + " not found."));
    }

    /**
     * Mark mapping for deletion by mapping id.
     *
     * @param mappingId mapping id
     */
    public void markForDeletion(UUID mappingId) {
        jpaMappingRepository.markForDeletion(mappingId);
    }

    /**
     * Mark all mappings for deletion by scope id.
     *
     * @param scopeId scope id
     */
    public void markForDeletionByScope(UUID scopeId) {
        final List<MappingEntity> mappingEntities =
                jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.unsorted());
        mappingEntities.forEach(mappingEntity -> mappingEntity.setDelete(true));
        jpaMappingRepository.saveAll(mappingEntities);
    }

    /**
     * Validate mapping.
     *
     * @param mappingId mapping id
     * @param mapping mapping {@link Map} key {@link String} = source, value {@link String}[] = targets
     * @param dataIntegrationHeaders {@link List} of {@link DataIntegrationHeaderDataAPIModel} (headers from host)
     */
    public void validateMapping(UUID mappingId, Map<String, String[]> mapping,
                                List<DataIntegrationHeaderDataAPIModel> dataIntegrationHeaders) {
        final String errorPrefix = "Mapping with id " + mappingId + " ";
        final Set<String> hostTargets = dataIntegrationHeaders.stream()
                .map(DataIntegrationHeaderDataAPIModel::getId)
                .collect(Collectors.toSet());

        final ValidateMappingModel validateMapping = mappingsValidationService.validateMapping(mapping, hostTargets);
        mappingsValidationService.validateMappingErrorHandler(errorPrefix, validateMapping);
    }

}
