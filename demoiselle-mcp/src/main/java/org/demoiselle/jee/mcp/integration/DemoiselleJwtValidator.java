/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;

/**
 * Fail-closed bridge from MCP authentication to Demoiselle's optional JWT
 * module. Reflection keeps {@code demoiselle-security-jwt} optional while CDI
 * still supplies its managed {@code JwtTokenValidator} when present.
 */
@Specializes
@ApplicationScoped
public class DemoiselleJwtValidator extends NoOpJwtValidator {

    private static final String VALIDATOR_TYPE =
            "org.demoiselle.jee.security.jwt.api.JwtTokenValidator";

    @Inject
    private BeanManager beanManager;

    @Override
    public JwtValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            return JwtValidationResult.invalid("Missing JWT token");
        }
        try {
            Object validator = resolveValidator();
            if (validator == null) {
                return JwtValidationResult.invalid("JWT validator unavailable");
            }
            Method validate = validator.getClass().getMethod("validate", String.class);
            Object validated = validate.invoke(validator, token);
            if (!(validated instanceof DemoiselleUser user)) {
                return JwtValidationResult.invalid("JWT did not resolve an authenticated principal");
            }
            String subject = user.getIdentity();
            if (subject == null || subject.isBlank()) {
                subject = user.getName();
            }
            if (subject == null || subject.isBlank()) {
                return JwtValidationResult.invalid("JWT principal has no stable identity");
            }
            return JwtValidationResult.ok(subject);
        } catch (InvocationTargetException e) {
            return JwtValidationResult.invalid(safeDetail(e.getCause()));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return JwtValidationResult.invalid("JWT validation failed");
        }
    }

    private Object resolveValidator() throws ReflectiveOperationException {
        if (beanManager == null) {
            return null;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> type = Class.forName(VALIDATOR_TYPE, true,
                loader != null ? loader : getClass().getClassLoader());
        Set<Bean<?>> beans = beanManager.getBeans(type);
        Bean<?> bean = beanManager.resolve(beans);
        if (bean == null) {
            return null;
        }
        return beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
    }

    private static String safeDetail(Throwable cause) {
        if (cause == null) {
            return "JWT validation failed";
        }
        String name = cause.getClass().getSimpleName();
        return name == null || name.isBlank() ? "JWT validation failed" : name;
    }
}
