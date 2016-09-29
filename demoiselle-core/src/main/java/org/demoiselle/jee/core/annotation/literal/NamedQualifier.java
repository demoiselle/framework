/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation.literal;

import java.lang.annotation.Annotation;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

/**
 * Annotation litteral that allows to create instances of the  literal.
 * Those instances can then be used to call
 *  
 * @author SERPRO
 */
@SuppressWarnings("all")
public class NamedQualifier extends AnnotationLiteral<Named> implements Named {

	private static final long serialVersionUID = 6790759427086052113L;

	private String namedValue;

	public NamedQualifier(String value) {
		namedValue = value;
	}

	public String value() {
		return namedValue;
	}
}
