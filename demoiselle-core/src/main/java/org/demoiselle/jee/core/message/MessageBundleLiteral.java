/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import jakarta.enterprise.util.AnnotationLiteral;

import org.demoiselle.jee.core.annotation.MessageBundle;

/**
 * Annotation literal for the {@link MessageBundle} qualifier,
 * used when programmatically registering CDI beans.
 *
 * @author SERPRO
 */
public class MessageBundleLiteral extends AnnotationLiteral<MessageBundle> implements MessageBundle {

    private static final long serialVersionUID = 1L;
}
