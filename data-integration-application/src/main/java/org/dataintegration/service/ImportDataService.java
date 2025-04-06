package org.dataintegration.service;

import com.opencsv.CSVReader;
import org.dataintegration.config.ImportDataBatchConfig;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Service to import data into the application.
 */
public interface ImportDataService {
    /**
     * Import data, currently limited to csv.
     * <p>
     * This will start an async resilient multi-threaded import process limited by the configuration of {@link ImportDataBatchConfig}.
     * The process is interruptible and can also be resumed later on if it is not finished yet. It is optimized to be fast by
     * ensuring fast database read/save operations (lazy loading/batch inserts configured).
     * It also has various log messages which are showing more or less information base on the log level.
     *
     * @param csvReaderCallable csv reader (callable)
     * @param projectId project id
     * @param scopeId scope id
     * @param lineCount line count (number of lines of csv file that will be imported)
     * @return true if import was successful without any errors, false if it failed (or part of it failed)
     */
    boolean importData(Callable<CSVReader> csvReaderCallable, UUID projectId, UUID scopeId, long lineCount);
}
