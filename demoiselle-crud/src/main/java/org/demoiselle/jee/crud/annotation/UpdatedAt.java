/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be automatically populated with the update timestamp
 * when the entity is merged. The {@code AuditEntityListener} sets the
 * annotated field to {@code LocalDateTime.now()} during {@code @PreUpdate}.
 *
 * <pre>
 * &#64;Entity
 * &#64;EntityListeners(AuditEntityListener.class)
 * public class MyEntity {
 *     &#64;UpdatedAt
 *     private LocalDateTime updatedAt;
 * }
 * </pre>
 *
 * @author SERPRO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpdatedAt {
}
