/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

//TODO JAVADOC
/**
 *
 * @author SERPRO
 */
public interface Token {

    /**
     * @return Key name
     */
    public String getKey();

    /**
     *
     * @param key Key name
     */
    public void setKey(String key);

    /**
     * @return Type name
     */
    public String getType();

    /**
     *
     * @param type Type name
     */
    //TODO usar enum
    public void setType(String type);
}
