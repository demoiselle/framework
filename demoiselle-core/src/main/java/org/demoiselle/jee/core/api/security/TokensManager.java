/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

import java.io.Serializable;

/**
 *
 * @author 70744416353
 */
public interface TokensManager {

    /**
     *
     * @return
     */
    public DemoisellePrincipal getUser();

    /**
     *
     * @param user
     */
    public void setUser(DemoisellePrincipal user);

    /**
     *
     * @return
     */
    public boolean validate();

}
