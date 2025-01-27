package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.model.MappedItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ItemMapper.class)
public interface MappedItemMapper {
    MappedItemModel mappedItemEntityToMappedItem(MappedItemEntity mappedItemEntity);
}
