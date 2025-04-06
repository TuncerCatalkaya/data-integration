package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.model.ItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {
    ItemModel itemEntityToItem(ItemEntity itemEntity);

}
