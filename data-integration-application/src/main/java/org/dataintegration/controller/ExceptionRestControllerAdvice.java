package org.dataintegration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.BucketNotFoundException;
import org.dataintegration.exception.InvalidDelimiterException;
import org.dataintegration.exception.InvalidUUIDException;
import org.dataintegration.exception.KeyNotFoundException;
import org.dataintegration.exception.TagNotFoundException;
import org.dataintegration.exception.checked.DataIntegrationCheckedException;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.exception.runtime.CheckpointNotFoundException;
import org.dataintegration.exception.runtime.DataIntegrationRuntimeException;
import org.dataintegration.exception.runtime.DatabaseNotFoundException;
import org.dataintegration.exception.runtime.HostDomainNotValidException;
import org.dataintegration.exception.runtime.HostNotFoundException;
import org.dataintegration.exception.runtime.HostNotValidException;
import org.dataintegration.exception.runtime.ItemNotFoundException;
import org.dataintegration.exception.runtime.MappedItemNotFoundException;
import org.dataintegration.exception.runtime.MappingNotFoundException;
import org.dataintegration.exception.runtime.MappingValidationException;
import org.dataintegration.exception.runtime.ProjectForbiddenException;
import org.dataintegration.exception.runtime.ProjectNotFoundException;
import org.dataintegration.exception.runtime.ScopeNotFinishedException;
import org.dataintegration.exception.runtime.ScopeNotFoundException;
import org.dataintegration.exception.runtime.ScopeValidationException;
import org.dataintegration.service.ExceptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionRestControllerAdvice {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExceptionService exceptionService;

    private static final Map<List<Class<? extends DataIntegrationRuntimeException>>, HttpStatus>
            RUNTIME_EXCEPTION_STATUS_MAPPING = Map.of(
            List.of(
                    ProjectNotFoundException.class,
                    ScopeNotFoundException.class,
                    ItemNotFoundException.class,
                    CheckpointNotFoundException.class,
                    HostNotFoundException.class,
                    DatabaseNotFoundException.class,
                    MappingNotFoundException.class,
                    MappedItemNotFoundException.class,
                    BucketNotFoundException.class,
                    KeyNotFoundException.class,
                    TagNotFoundException.class
            ), HttpStatus.NOT_FOUND,
            List.of(ProjectForbiddenException.class), HttpStatus.FORBIDDEN,
            List.of(
                    InvalidUUIDException.class,
                    InvalidDelimiterException.class,
                    HostNotValidException.class
            ), HttpStatus.BAD_REQUEST,
            List.of(ScopeNotFinishedException.class), HttpStatus.TOO_EARLY,
            List.of(
                    ScopeValidationException.class,
                    MappingValidationException.class
            ), HttpStatus.CONFLICT,
            List.of(HostDomainNotValidException.class), HttpStatus.UNPROCESSABLE_ENTITY
    );

    private static final Map<List<Class<? extends DataIntegrationCheckedException>>, HttpStatus>
            CHECKED_EXCEPTION_STATUS_MAPPING = Map.of(
            List.of(ScopeHeaderValidationException.class), HttpStatus.CONFLICT
    );

    @ExceptionHandler({DataIntegrationRuntimeException.class, DataIntegrationCheckedException.class})
    ResponseEntity<String> handleDataIntegrationException(Exception ex) throws JsonProcessingException {
        HttpStatus status;

        if (ex instanceof DataIntegrationRuntimeException runtimeException) {
            status = exceptionService.mapStatus(runtimeException, RUNTIME_EXCEPTION_STATUS_MAPPING);
        } else if (ex instanceof DataIntegrationCheckedException checkedException) {
            status = exceptionService.mapStatus(checkedException, CHECKED_EXCEPTION_STATUS_MAPPING);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(OBJECT_MAPPER.writeValueAsString(ex.getMessage()), status);
    }

}
