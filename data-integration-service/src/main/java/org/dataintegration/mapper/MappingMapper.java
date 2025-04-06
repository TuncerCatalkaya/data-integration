package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.model.MappingModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {HostMapper.class, ScopeMapper.class})
public interface MappingMapper {
    MappingModel mappingEntityToMapping(MappingEntity mappingEntity);

}
