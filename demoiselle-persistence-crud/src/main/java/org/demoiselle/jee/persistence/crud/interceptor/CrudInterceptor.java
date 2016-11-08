/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud.interceptor;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.demoiselle.jee.persistence.crud.annotation.Crud;

@Crud
@Interceptor
@Priority(APPLICATION)
public class CrudInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        return ic.proceed();
    }
}
