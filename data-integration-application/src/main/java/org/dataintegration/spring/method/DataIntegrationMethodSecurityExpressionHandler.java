package org.dataintegration.spring.method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * Custom Data Integration {@link DefaultMethodSecurityExpressionHandler}.
 */
public class DataIntegrationMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        final StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        final DataIntegrationMethodSecurityExpressionRoot root = new DataIntegrationMethodSecurityExpressionRoot(authentication);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        root.setRoleHierarchy(getRoleHierarchy());
        context.setRootObject(root);
        return context;
    }
}