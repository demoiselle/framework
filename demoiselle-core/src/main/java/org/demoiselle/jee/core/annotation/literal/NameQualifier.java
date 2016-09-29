/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation.literal;

import javax.enterprise.util.AnnotationLiteral;
import org.demoiselle.jee.core.annotation.Name;

/**
 * Annotation litteral that allows to create instances of the literal. The created instance can then be
 *
 * @author SERPRO
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
