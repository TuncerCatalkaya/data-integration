package org.dataintegration.exception;

import lombok.experimental.StandardException;
import org.dataintegration.exception.runtime.DataIntegrationRuntimeException;

@StandardException
public class BucketNotFoundException extends DataIntegrationRuntimeException {
    public BucketNotFoundException(String bucket) {
        super("Bucket " + bucket + " could not be found.");
    }
}
