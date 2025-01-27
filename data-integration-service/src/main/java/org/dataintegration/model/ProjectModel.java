package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectModel {

    private UUID id;
    private String name;
    private String createdBy;
    private Date createdDate;
    private Date lastModifiedDate;

}
