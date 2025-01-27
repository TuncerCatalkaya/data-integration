package org.dataintegration.usecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dataintegration.model.HeaderModel;

import java.util.LinkedHashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateScopeHeadersRequestModel {

    private LinkedHashSet<HeaderModel> headers;

}
