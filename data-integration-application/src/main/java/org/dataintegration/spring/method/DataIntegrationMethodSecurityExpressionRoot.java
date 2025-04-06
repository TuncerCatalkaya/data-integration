package org.dataintegration.spring.method;

import lombok.Generated;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Custom Data Integration security {@link SecurityExpressionRoot}.
 */
public class DataIntegrationMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;

    public DataIntegrationMethodSecurityExpressionRoot(Supplier<Authentication> authentication) {
        super(authentication);
    }

    public boolean hasRegexAuthority(String... authorityRegexes) {
        final Collection<? extends GrantedAuthority> userAuthorities = getAuthentication().getAuthorities();
        final Set<String> authoritySet = AuthorityUtils.authorityListToSet(userAuthorities);

        final List<Pattern> patterns = Arrays.stream(authorityRegexes)
                .map(Pattern::compile)
                .toList();

        return authoritySet.stream()
                .anyMatch(authority -> patterns.stream().anyMatch(pattern -> pattern.matcher(authority).matches()));
    }

    @Generated
    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Generated
    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Generated
    @Override
    public Object getThis() {
        return this;
    }

    @Generated
    @Override
    public void setFilterObject(Object obj) {
        this.filterObject = obj;
    }

    @Generated
    @Override
    public void setReturnObject(Object obj) {
        this.returnObject = obj;
    }
}
