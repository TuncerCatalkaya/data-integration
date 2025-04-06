package org.dataintegration.service;

import org.dataintegration.model.CorsConfigurationPropertiesModel;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private CorsConfigurationPropertiesModel corsConfigurationPropertiesModel;

    @InjectMocks
    private UrlService subject;

    @ParameterizedTest
    @MethodSource
    void testIsDomainValid(String url, boolean expected) {
        when(corsConfigurationPropertiesModel.getAllowedOriginPatterns()).thenReturn(List.of(
                "http://localhost*",
                "http://someDomainThatShouldBeAllowed"
        ));

        final boolean result = subject.isDomainValid(url);

        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> testIsDomainValid() {
        return Stream.of(
                Arguments.of("http://localhost:8080", true),
                Arguments.of("http://localhost:8000", true),
                Arguments.of("http://someDomainThatShouldBeAllowed", true),
                Arguments.of("http://someEvilDomain", false)
        );
    }

}