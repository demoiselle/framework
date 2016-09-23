/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation.literal;

import org.demoiselle.jee.core.annotation.Type;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation litteral that allows to create instances of the {@link Type}
 * literal. The created instance can then be used to call
 * {@link javax.enterprise.inject.spi.CDI#select(Class subtype, java.lang.annotation.Annotation... qualifiers)}.
 *
 * @see javax.enterprise.inject.spi.CDI
 * @author SERPRO
 */
@SuppressWarnings("all")
public class TypeQualifier extends AnnotationLiteral<Type> implements Type {

    private static final long serialVersionUID = 1L;

    private final Class<?> value;

    /**
     * Constructor with string value of name literal.
     *
     * @param value value of name literal.
     */
    public TypeQualifier(Class<?> value) {
        this.value = value;
    }

    @Override
    public Class<?> value() {
        return this.value;
    }
}
