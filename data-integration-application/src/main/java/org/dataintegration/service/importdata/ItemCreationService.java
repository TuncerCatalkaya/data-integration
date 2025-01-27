package org.dataintegration.service.importdata;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.service.ScopesService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
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

    ItemEntity createItemEntity(String line, ScopeEntity scopeEntity, LinkedHashSet<HeaderModel> headers, long lineNumber, char delimiter) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setLineNumber(lineNumber);
        itemEntity.setProperties(getProperties(line, headers, delimiter));
        return itemEntity;
    }

    private Map<String, ItemPropertiesModel> getProperties(String line, LinkedHashSet<HeaderModel> headers, char delimiter) {
        final Map<String, ItemPropertiesModel> properties = new WeakHashMap<>(headers.size());
        final List<String> fields = Splitter.on(delimiter).splitToList(line);
        int i = 0;
        for (HeaderModel header : headers) {
            properties.put(header.getName(), ItemPropertiesModel.builder()
                    .value(fields.get(i++))
                    .build());
        }
        return properties;
    }
}
