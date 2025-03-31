package org.dataintegration.config.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class AuthorityConfig {

    @Value("${authority.regexes}")
    private List<String> authorityRegexes;

}
