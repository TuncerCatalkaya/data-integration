package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.model.HostModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = DatabaseMapper.class)
public interface HostMapper {
    @Mapping(target = "inUse", expression = """
        java(hostEntity.getDatabases().stream().anyMatch(database -> !database.getMappings().isEmpty()))
    """)
    HostModel hostEntityToHost(HostEntity hostEntity);
    HostEntity hostToHostEntity(HostModel host);
}
