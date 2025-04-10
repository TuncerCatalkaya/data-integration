package org.dataintegration.exception;

import lombok.experimental.StandardException;
import org.dataintegration.exception.runtime.DataIntegrationRuntimeException;

@StandardException
public class InvalidUUIDException extends DataIntegrationRuntimeException {
}
