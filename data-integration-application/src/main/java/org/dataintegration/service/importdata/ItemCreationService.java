package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.service.ScopesService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;

@Service
@RequiredArgsConstructor
class ItemCreationService {

    private final ScopesService scopesService;

    boolean isHeaderValid(LinkedHashSet<HeaderModel> headers) {
        try {
            scopesService.validateHeaders(headers);
        } catch (ScopeHeaderValidationException e) {
            return false;
        }
        return true;
    }

    ItemEntity createItemEntity(String[] line, ScopeEntity scopeEntity, long lineNumber) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setLineNumber(lineNumber);
        itemEntity.setProperties(getProperties(line, scopeEntity.getHeaders()));
        return itemEntity;
    }

    private Map<String, ItemPropertiesModel> getProperties(String[] line, LinkedHashSet<HeaderModel> headers) {
        final Map<String, ItemPropertiesModel> properties = new WeakHashMap<>(headers.size());
        int i = 0;
        for (HeaderModel header : headers) {
            properties.put(header.getId(), ItemPropertiesModel.builder()
                    .value(line[i++])
                    .build());
        }
        return properties;
    }
}
