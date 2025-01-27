package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.mapper.MappedItemMapper;
import org.dataintegration.model.MappedItemModel;
import org.dataintegration.service.MappedItemsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.usecase.api.MappedItemsMethods;
import org.dataintegration.usecase.model.ApplyUnmappingRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
class MappedItems implements MappedItemsMethods {

    private final MappedItemMapper mappedItemMapper = Mappers.getMapper(MappedItemMapper.class);
    private final ProjectsService projectsService;
    private final MappedItemsService mappedItemsService;

    public Page<MappedItemModel> getAllMappedItems(UUID projectId, UUID mappingId, String createdBy, Pageable pageable) {
        projectsService.isPermitted(projectId, createdBy);
        final Page<MappedItemEntity> mappedItemEntities = mappedItemsService.getByMapping(mappingId, pageable);
        final List<MappedItemModel> mappedItems = mappedItemEntities.stream()
                .map(mappedItemMapper::mappedItemEntityToMappedItem)
                .toList();
        return new PageImpl<>(mappedItems, mappedItemEntities.getPageable(), mappedItemEntities.getTotalElements());
    }

    public MappedItemModel updateMappedItemProperty(UUID projectId, UUID mappedItemId, String key, String newValue,
                                                    String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final MappedItemEntity mappedItemEntity = mappedItemsService.updateMappedItemProperty(mappedItemId, key, newValue);
        return mappedItemMapper.mappedItemEntityToMappedItem(mappedItemEntity);
    }

    public void deleteMappedItems(UUID projectId, ApplyUnmappingRequestModel applyUnmappingRequest, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        mappedItemsService.deleteMappedItems(applyUnmappingRequest.getMappedItemIds());
    }

}
