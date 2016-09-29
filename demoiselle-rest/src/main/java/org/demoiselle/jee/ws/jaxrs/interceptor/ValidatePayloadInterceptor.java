/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.ws.jaxrs.interceptor;

import org.demoiselle.jee.ws.jaxrs.annotation.ValidatePayload;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Priority;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.UnexpectedTypeException;
import javax.validation.Validation;
import static javax.validation.Validation.buildDefaultValidatorFactory;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.demoiselle.jee.ws.jaxrs.exception.DemoiselleRESTException;

@Interceptor
@ValidatePayload
@Priority(APPLICATION)
public class ValidatePayloadInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        DemoiselleRESTException ex = new DemoiselleRESTException();
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        for (Object params : ic.getParameters()) {
            if (params != null) {
                ValidatorFactory dfv = buildDefaultValidatorFactory();
                Validator validator = dfv.getValidator();
                try {
                    violations.addAll(validator.validate(params));
                    for (ConstraintViolation<?> violation : violations) {
                        String field = (violation.getRootBeanClass().getSimpleName() + "_"
                                + violation.getPropertyPath()).toLowerCase();
                        // GPMessage msg =
                        // GPMessage.INVALID_FIELD_P1.setSufix(violation.getConstraintDescriptor()
                        // .getAnnotation().annotationType().getSimpleName().toLowerCase());

                        ex.addMessage(field, violation.getMessage());
                    }
                } catch (UnexpectedTypeException cause) {
                    // GPMessage msg = GPMessage.GENERAL_ERROR_P1;
                    // msg.setParam(cause.getMessage());
                    throw new DemoiselleRESTException("ERRO GENERICO -> ALTERAR");
                }
            }
        }

        if (!violations.isEmpty() && !ex.getMessages().isEmpty()) {
            throw ex;
        }
        return ic.proceed();
    }
}
