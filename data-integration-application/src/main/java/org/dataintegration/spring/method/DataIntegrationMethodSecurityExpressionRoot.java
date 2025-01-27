package org.dataintegration.spring.method;

import lombok.Generated;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Custom Data Integration security {@link SecurityExpressionRoot}.
 */
public class DataIntegrationMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;

    public DataIntegrationMethodSecurityExpressionRoot(Supplier<Authentication> authentication) {
        super(authentication);
    }

    /**
     * Method security to check authorities with given tenant.
     *
     * @param tenant tenant that should be checked
     * @param authorities authorities that should be checked
     * @return true if authorized on passed tenant
     */
    public boolean hasAnyMultiTenantAuthority(String tenant, String... authorities) {
//        final String[] bdwAuthorities = Arrays.stream(authorities)
//                .map(authority -> atlas + ATLAS_AUTHORITY_SPLIT_CHAR + authority)
//                .toArray(String[]::new);
//        return hasAnyAuthority(bdwAuthorities);
        // TODO: write logic to enable custom built format
        return false;
    }

    /**
     * Method security to check if contains at leat one of authorities.
     *
     * @param authorities authorities that should be checked if included
     * @return true if contains one of the authorities
     */
    public boolean containsAnyAuthority(String... authorities) {
        final Collection<? extends GrantedAuthority> userAuthorities = getAuthentication().getAuthorities();
        final Set<String> authoritySet = AuthorityUtils.authorityListToSet(userAuthorities);
        return Arrays.stream(authorities).anyMatch(authority -> authoritySet.stream().anyMatch(a -> a.contains(authority)));
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
