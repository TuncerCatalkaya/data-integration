package org.dataintegration.model.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class DataIntegrationCache {

    private final Set<UUID> interruptingScopes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> processingScopes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> markedForDeletionScopes = ConcurrentHashMap.newKeySet();

}
