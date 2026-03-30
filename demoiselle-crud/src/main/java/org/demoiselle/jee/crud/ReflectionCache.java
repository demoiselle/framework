/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Application-scoped cache for reflection metadata.
 * Avoids repeated reflection calls for the same entity class by caching
 * the mapping of field name to {@link Field} for each class.
 *
 * <p>Thread-safe by design via {@link ConcurrentHashMap} and
 * {@link ConcurrentHashMap#computeIfAbsent}.</p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class ReflectionCache {

    private final ConcurrentHashMap<Class<?>, Map<String, Field>> cache = new ConcurrentHashMap<>();

    /**
     * Returns an unmodifiable map of field name → {@link Field} for the given class,
     * traversing the entire class hierarchy (including superclasses).
     *
     * @param clazz the class to inspect
     * @return unmodifiable map of field name to Field
     */
    public Map<String, Field> getFields(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, this::buildFieldMap);
    }

    /**
     * Builds the field map for a class by traversing its entire hierarchy.
     */
    private Map<String, Field> buildFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field field : CrudUtilHelper.getAllFields(new ArrayList<>(), clazz)) {
            fieldMap.putIfAbsent(field.getName(), field);
        }
        return Collections.unmodifiableMap(fieldMap);
    }
}
