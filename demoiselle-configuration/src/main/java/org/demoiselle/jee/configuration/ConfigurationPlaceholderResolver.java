/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves property placeholders such as {@code ${env:VAR}},
 * {@code ${sys:prop}} and recursive fallbacks with {@code :-}.
 */
public final class ConfigurationPlaceholderResolver {

    private static final int MAX_DEPTH = 10;

    private ConfigurationPlaceholderResolver() {
    }

    public static String resolve(String configuredValue) {
        if (configuredValue == null) {
            return null;
        }
        return resolve(configuredValue, 0);
    }

    public static Object toArray(Object configuredValue, Class<?> componentType) throws ClassNotFoundException {
        List<Object> values = new ArrayList<>();

        if (configuredValue == null) {
            return null;
        }

        if (configuredValue.getClass().isArray()) {
            int length = Array.getLength(configuredValue);
            for (int i = 0; i < length; i++) {
                appendResolvedValues(values, String.valueOf(Array.get(configuredValue, i)), componentType);
            }
        } else if (configuredValue instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                appendResolvedValues(values, item == null ? null : String.valueOf(item), componentType);
            }
        } else {
            appendResolvedValues(values, String.valueOf(configuredValue), componentType);
        }

        if (values.isEmpty()) {
            return null;
        }

        Object array = Array.newInstance(componentType, values.size());
        for (int i = 0; i < values.size(); i++) {
            Array.set(array, i, values.get(i));
        }
        return array;
    }

    public static Object convert(String value, Class<?> targetType) throws ClassNotFoundException {
        String resolved = resolve(value);
        if (resolved == null) {
            return null;
        }

        Class<?> wrappedType = wrap(targetType);

        if (wrappedType == String.class) {
            return resolved;
        }
        if (wrappedType == Boolean.class) {
            if ("true".equalsIgnoreCase(resolved) || "false".equalsIgnoreCase(resolved)) {
                return Boolean.valueOf(resolved);
            }
            throw new IllegalArgumentException(
                    "Invalid boolean value: '" + resolved + "'. Expected 'true' or 'false'.");
        }
        if (wrappedType == Byte.class) {
            return Byte.valueOf(resolved);
        }
        if (wrappedType == Short.class) {
            return Short.valueOf(resolved);
        }
        if (wrappedType == Integer.class) {
            return Integer.valueOf(resolved);
        }
        if (wrappedType == Long.class) {
            return Long.valueOf(resolved);
        }
        if (wrappedType == Float.class) {
            return Float.valueOf(resolved);
        }
        if (wrappedType == Double.class) {
            return Double.valueOf(resolved);
        }
        if (wrappedType == Character.class) {
            if (resolved.length() != 1) {
                throw new IllegalArgumentException("Character configuration must contain a single character.");
            }
            return resolved.charAt(0);
        }
        if (wrappedType == Class.class) {
            return Class.forName(resolved, true, getClassLoader());
        }
        if (wrappedType.isEnum()) {
            Object[] constants = wrappedType.getEnumConstants();
            for (Object constant : constants) {
                if (((Enum<?>) constant).name().equals(resolved)) {
                    return constant;
                }
            }
            return null;
        }

        return resolved;
    }

    private static void appendResolvedValues(List<Object> values, String rawValue, Class<?> componentType) throws ClassNotFoundException {
        String resolved = resolve(rawValue);
        if (resolved == null) {
            return;
        }

        for (String item : split(resolved)) {
            values.add(convert(item, componentType));
        }
    }

    private static List<String> split(String value) {
        List<String> items = new ArrayList<>();
        for (String item : value.split(",")) {
            String normalized = normalize(item);
            if (normalized != null) {
                items.add(normalized);
            }
        }
        return items;
    }

    private static String resolve(String configuredValue, int depth) {
        if (configuredValue == null) {
            return null;
        }

        String trimmed = configuredValue.trim();

        if (depth > MAX_DEPTH) {
            return trimmed;
        }

        if (!trimmed.startsWith("${") || !trimmed.endsWith("}")) {
            return configuredValue;
        }

        String expression = trimmed.substring(2, trimmed.length() - 1).trim();
        if (expression.startsWith("env:")) {
            return resolveLookup(expression.substring(4), true, depth + 1);
        }
        if (expression.startsWith("sys:")) {
            return resolveLookup(expression.substring(4), false, depth + 1);
        }

        return trimmed;
    }

    private static String resolveLookup(String expression, boolean environmentLookup, int depth) {
        String key = expression;
        String fallback = null;

        int separator = findTopLevelFallbackSeparator(expression);
        if (separator >= 0) {
            key = expression.substring(0, separator);
            fallback = expression.substring(separator + 2);
        }

        String resolved = environmentLookup
                ? normalize(System.getenv(normalize(key)))
                : normalize(System.getProperty(normalize(key)));

        if (resolved != null) {
            return resolved;
        }

        return resolve(fallback, depth);
    }

    private static int findTopLevelFallbackSeparator(String expression) {
        int nestedPlaceholders = 0;

        for (int i = 0; i < expression.length() - 1; i++) {
            char current = expression.charAt(i);
            char next = expression.charAt(i + 1);

            if (current == '$' && next == '{') {
                nestedPlaceholders++;
                i++;
                continue;
            }

            if (current == '}' && nestedPlaceholders > 0) {
                nestedPlaceholders--;
                continue;
            }

            if (current == ':' && next == '-' && nestedPlaceholders == 0) {
                return i;
            }
        }

        return -1;
    }

    private static Class<?> wrap(Class<?> type) {
        if (type == null || !type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? ConfigurationPlaceholderResolver.class.getClassLoader() : classLoader;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
