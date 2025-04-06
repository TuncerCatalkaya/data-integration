package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.ScopeModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScopeMapper {
    ScopeModel scopeEntityToScope(ScopeEntity scopeEntity);

}
