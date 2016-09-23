/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation.literal;

import org.demoiselle.jee.core.annotation.Name;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation litteral that allows to create instances of the {@link Name} literal. The created instance can then be
 * used to call {@link javax.enterprise.inject.spi.CDI#select(Class subtype, java.lang.annotation.Annotation... qualifiers)}
 *
 * @author SERPRO
 * @see javax.enterprise.inject.spi.CDI
 */
@SuppressWarnings("all")
public class NameQualifier extends AnnotationLiteral<Name> implements Name {

	private static final long serialVersionUID = 1L;

	private final String value;

	/**
	 * Constructor with string value of name literal.
	 *
	 * @param value value of name literal.
	 */
	public NameQualifier(String value) {
		this.value = value;
	}

	@Override
	public String value() {
		return this.value;
	}
}
