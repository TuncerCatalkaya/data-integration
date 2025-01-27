package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    public <T> HttpStatus mapStatus(T ex, Map<List<Class<? extends T>>, HttpStatus> exceptionMapping) {
        return exceptionMapping.entrySet().stream()
                .filter(entry -> entry.getKey().contains(ex.getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
