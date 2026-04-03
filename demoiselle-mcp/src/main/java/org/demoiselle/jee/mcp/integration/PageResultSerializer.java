/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializes page result objects with pagination metadata for MCP responses.
 *
 * <p>This class is registered conditionally when {@code demoiselle-crud} is on
 * the classpath. It uses reflection to extract pagination metadata from the
 * page result object, avoiding a hard compile-time dependency on the optional
 * {@code demoiselle-crud} module.</p>
 *
 * <p>The serialized output includes:</p>
 * <ul>
 *   <li>{@code content} — the page items as an array</li>
 *   <li>{@code totalElements}, {@code totalPages}, {@code currentPage},
 *       {@code pageSize}, {@code hasNext}, {@code hasPrevious} — pagination metadata</li>
 *   <li>{@code nextCursor} — present only when {@code hasNext} is {@code true}</li>
 * </ul>
 */
public class PageResultSerializer {

    private static final Logger LOG = Logger.getLogger(PageResultSerializer.class.getName());

    /**
     * Serializes a page result object into a map with content and pagination metadata.
     *
     * @param pageResult the page result object (expected to have getter methods
     *                   for pagination fields)
     * @return a map containing {@code content} (List) and pagination metadata,
     *         or an empty map if the object cannot be introspected
     * @throws NullPointerException if {@code pageResult} is {@code null}
     */
    public Map<String, Object> serialize(Object pageResult) {
        if (pageResult == null) {
            throw new NullPointerException("pageResult must not be null");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // Extract content
        Object content = invokeGetter(pageResult, "getContent");
        if (content == null) {
            content = invokeGetter(pageResult, "content");
        }
        if (content instanceof Collection<?>) {
            result.put("content", List.copyOf((Collection<?>) content));
        } else if (content != null) {
            result.put("content", content);
        } else {
            result.put("content", List.of());
        }

        // Extract pagination metadata
        putIfPresent(result, "totalElements", pageResult, "getTotalElements", "totalElements");
        putIfPresent(result, "totalPages", pageResult, "getTotalPages", "totalPages");
        putIfPresent(result, "currentPage", pageResult, "getCurrentPage", "currentPage");
        putIfPresent(result, "pageSize", pageResult, "getPageSize", "pageSize");

        Boolean hasNext = getBooleanValue(pageResult, "isHasNext", "getHasNext", "hasNext");
        Boolean hasPrevious = getBooleanValue(pageResult, "isHasPrevious", "getHasPrevious", "hasPrevious");

        if (hasNext != null) {
            result.put("hasNext", hasNext);
        }
        if (hasPrevious != null) {
            result.put("hasPrevious", hasPrevious);
        }

        // Include nextCursor only when hasNext is true
        if (Boolean.TRUE.equals(hasNext)) {
            Object currentPage = result.get("currentPage");
            if (currentPage instanceof Number) {
                result.put("nextCursor", String.valueOf(((Number) currentPage).intValue() + 1));
            } else {
                result.put("nextCursor", "next");
            }
        }

        return result;
    }

    /**
     * Checks whether the given object looks like a page result by testing
     * for the presence of pagination getter methods.
     *
     * @param obj the object to test
     * @return {@code true} if the object has pagination-related methods
     */
    public boolean isPageResult(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        // Check for at least getContent + one pagination method
        boolean hasContent = hasMethod(clazz, "getContent") || hasMethod(clazz, "content");
        boolean hasPagination = hasMethod(clazz, "getTotalElements") || hasMethod(clazz, "totalElements")
                || hasMethod(clazz, "getTotalPages") || hasMethod(clazz, "totalPages");
        return hasContent && hasPagination;
    }

    // ── Internal helpers ──

    private void putIfPresent(Map<String, Object> map, String key,
                              Object target, String... methodNames) {
        for (String methodName : methodNames) {
            Object value = invokeGetter(target, methodName);
            if (value != null) {
                map.put(key, value);
                return;
            }
        }
    }

    private Boolean getBooleanValue(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            Object value = invokeGetter(target, methodName);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }

    private Object invokeGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException e) {
            // Method not found — expected for optional fields
            return null;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to invoke " + methodName + " on "
                    + target.getClass().getSimpleName(), e);
            return null;
        }
    }

    private boolean hasMethod(Class<?> clazz, String methodName) {
        try {
            clazz.getMethod(methodName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
