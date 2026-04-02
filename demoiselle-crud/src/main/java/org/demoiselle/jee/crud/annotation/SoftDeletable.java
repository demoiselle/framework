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
import java.time.LocalDateTime;

/**
 * Marks an entity for soft delete behavior. When an entity annotated with
 * {@code @SoftDeletable} is removed via {@code AbstractDAO.remove()}, the
 * framework executes a JPA UPDATE setting the specified field instead of a
 * physical DELETE.
 *
 * <p>Supported field types: {@link LocalDateTime}, {@link java.time.Instant},
 * and {@link Boolean}.</p>
 *
 * <pre>
 * &#64;Entity
 * &#64;SoftDeletable(field = "deletedAt")
 * public class MyEntity {
 *     private LocalDateTime deletedAt;
 *     // ...
 * }
 * </pre>
 *
 * @author SERPRO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SoftDeletable {

    /**
     * The name of the entity field used to mark logical deletion.
     *
     * @return the soft delete field name
     */
    String field();

    /**
     * The type of the soft delete field. Defaults to {@link LocalDateTime}.
     * Supported types: {@link LocalDateTime}, {@link java.time.Instant},
     * {@link Boolean}.
     *
     * @return the soft delete field type
     */
    Class<?> type() default LocalDateTime.class;
}
