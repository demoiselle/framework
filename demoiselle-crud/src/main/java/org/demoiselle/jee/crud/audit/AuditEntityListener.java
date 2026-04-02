/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.audit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.annotation.CreatedAt;
import org.demoiselle.jee.crud.annotation.CreatedBy;
import org.demoiselle.jee.crud.annotation.UpdatedAt;
import org.demoiselle.jee.crud.annotation.UpdatedBy;

/**
 * JPA EntityListener that automatically populates audit fields annotated with
 * {@link CreatedAt}, {@link CreatedBy}, {@link UpdatedAt} and {@link UpdatedBy}.
 *
 * <p>Register this listener on your entity via
 * {@code @EntityListeners(AuditEntityListener.class)}.</p>
 *
 * <p>CDI injection of {@link DemoiselleUser} is used to resolve the current
 * user identity. When no user context is available (e.g. batch operations
 * without an HTTP request), the fallback value {@code "system"} is used.</p>
 *
 * @author SERPRO
 */
public class AuditEntityListener {

    private static final Logger LOG = Logger.getLogger(AuditEntityListener.class.getName());

    @Inject
    private DemoiselleUser demoiselleUser;

    @PrePersist
    public void onPrePersist(Object entity) {
        setFieldIfAnnotated(entity, CreatedAt.class, LocalDateTime.now());
        setFieldIfAnnotated(entity, CreatedBy.class, resolveUser());
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        setFieldIfAnnotated(entity, UpdatedAt.class, LocalDateTime.now());
        setFieldIfAnnotated(entity, UpdatedBy.class, resolveUser());
    }

    String resolveUser() {
        if (demoiselleUser != null && demoiselleUser.getIdentity() != null) {
            return demoiselleUser.getIdentity();
        }
        return "system";
    }

    private void setFieldIfAnnotated(Object entity, Class<? extends Annotation> annotation, Object value) {
        for (Field field : CrudUtilHelper.getAllFields(new ArrayList<>(), entity.getClass())) {
            if (field.isAnnotationPresent(annotation)) {
                field.setAccessible(true);
                try {
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    LOG.log(Level.WARNING, "Failed to set audit field: " + field.getName(), e);
                }
            }
        }
    }
}
