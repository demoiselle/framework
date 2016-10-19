
package org.demoiselle.jee.core.annotation.literal;

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

	private static final long serialVersionUID = 6_790_759_427_086_052_113L;

	private String namedValue;

    /**
     *
     * @param value
     */
    public NamedQualifier(String value) {
		namedValue = value;
	}

    /**
     *
     * @return
     */
    @Override
    public String value() {
		return namedValue;
	}
}
