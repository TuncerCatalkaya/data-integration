package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.model.DatabaseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatabaseMapper {
    @Mapping(target = "inUse", expression = "java(!databaseEntity.getMappings().isEmpty())")
    DatabaseModel databaseEntityToDatabase(DatabaseEntity databaseEntity);
    DatabaseEntity databaseToDatabaseEntity(DatabaseModel database);
}
