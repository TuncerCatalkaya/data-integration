package org.dataintegration.usecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3ListResponseModel {

    private String key;
    private Date lastModified;
    private Long size;
    private boolean checkpoint;

}
