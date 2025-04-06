package org.dataintegration.service;

import org.dataintegration.exception.runtime.MappingValidationException;
import org.dataintegration.model.ValidateMappingModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class MappingsValidationServiceTest {

    @InjectMocks
    private MappingsValidationService subject;

    @Test
    void testValidateMapping() {
        final Map<String, String[]> mapping = Map.of(
                "source12", new String[]{"target1", ""},
                "source3456", new String[]{"target3", "target4", "target5", "target6"},
                "source56", new String[]{"target5", "target6"},
                "source67", new String[]{"target6", "target7"}
        );

        final Set<String> hostTargets = Set.of("target1", "target4", "target7");

        final ValidateMappingModel result = subject.validateMapping(mapping, hostTargets);

        assertThat(result.getDuplicatedValues()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        "source3456", Set.of("target5", "target6"),
                        "source56", Set.of("target5", "target6"),
                        "source67", Set.of("target6")
                )
        );
        assertThat(result.getNamesNotInHost()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        "source3456", Set.of("target3", "target5", "target6"),
                        "source56", Set.of("target5", "target6"),
                        "source67", Set.of("target6")
                )
        );
        assertThat(result.getEmptyValues()).containsExactly("source12");
    }

    @Test
    void testValidateMappingErrorHandler() {
        assertThatNoException().isThrownBy(() -> subject.validateMappingErrorHandler("errorPrefix", ValidateMappingModel.builder()
                .duplicatedValues(Map.of())
                .namesNotInHost(Map.of())
                .emptyValues(Set.of())
                .build()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testValidateMappingErrorHandlerMappingValidationException(String testName, ValidateMappingModel validateMapping) {
        assertThatExceptionOfType(MappingValidationException.class).isThrownBy(() ->
                subject.validateMappingErrorHandler("errorPrefix", validateMapping));
    }

    private static Stream<Arguments> testValidateMappingErrorHandlerMappingValidationException() {
        return Stream.of(
                Arguments.of("Only duplicated values", ValidateMappingModel.builder()
                        .duplicatedValues(Map.of("source1", Set.of("target1", "target2")))
                        .namesNotInHost(Map.of())
                        .emptyValues(Set.of())
                        .build()),
                Arguments.of("Only names not in host", ValidateMappingModel.builder()
                        .duplicatedValues(Map.of())
                        .namesNotInHost(Map.of("source1", Set.of("target1", "target2")))
                        .emptyValues(Set.of())
                        .build()),
                Arguments.of("Only empty value", ValidateMappingModel.builder()
                        .duplicatedValues(Map.of())
                        .namesNotInHost(Map.of())
                        .emptyValues(Set.of("source1", "source2"))
                        .build())
        );
    }

}