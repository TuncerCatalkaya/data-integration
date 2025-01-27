package org.dataintegration.mapper;

import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.model.ProjectModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    ProjectModel projectEntityToProject(ProjectEntity projectEntity);
}
