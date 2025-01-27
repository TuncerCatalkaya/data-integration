package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.usecase.model.CreateOrUpdateHostsRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreateOrUpdateHostsMapper {
    HostEntity createOrUpdateHostsToHostEntity(CreateOrUpdateHostsRequestModel createOrUpdateHostsRequest);
}
