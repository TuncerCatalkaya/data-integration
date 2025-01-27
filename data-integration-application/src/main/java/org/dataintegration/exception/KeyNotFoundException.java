package org.dataintegration.exception;

import lombok.experimental.StandardException;
import org.dataintegration.exception.runtime.DataIntegrationRuntimeException;

@StandardException
public class KeyNotFoundException extends DataIntegrationRuntimeException {
    public KeyNotFoundException(String key) {
        super("Key " + key + " could not be found.");
    }
}
