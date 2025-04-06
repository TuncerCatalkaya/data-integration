package org.dataintegration.logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchProcessingLogger {

    public static void log(Level level, String scopeKey, UUID scopeId, String msg) {
        log.atLevel(level).log(scopeKey + "(" + scopeId + "), " + msg);
    }

}
