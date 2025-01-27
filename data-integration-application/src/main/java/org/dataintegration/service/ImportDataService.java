package org.dataintegration.service;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface ImportDataService {
    boolean importData(Callable<InputStream> inputStreamCallable, UUID projectId, UUID scopeId, long lineCount, char delimiter);
}
