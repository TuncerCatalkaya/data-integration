package org.dataintegration.service;

import com.opencsv.CSVReader;

import java.util.UUID;
import java.util.concurrent.Callable;

public interface ImportDataService {
    boolean importData(Callable<CSVReader> csvReaderCallable, UUID projectId, UUID scopeId, long lineCount);
}
