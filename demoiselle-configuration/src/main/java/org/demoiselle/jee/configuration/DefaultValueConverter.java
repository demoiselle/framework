/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

/**
 * Utility class responsible for converting a {@link String} value from the
 * {@link org.demoiselle.jee.configuration.annotation.DefaultValue @DefaultValue}
 * annotation into the target field type.
 *
 * <p>Supported target types:</p>
 * <ul>
 *   <li>{@code String} — returned as-is</li>
 *   <li>Primitives and their wrappers ({@code int}/{@code Integer},
 *       {@code long}/{@code Long}, {@code double}/{@code Double},
 *       {@code boolean}/{@code Boolean}, {@code float}/{@code Float},
 *       {@code short}/{@code Short}, {@code byte}/{@code Byte},
 *       {@code char}/{@code Character})</li>
 *   <li>Enum types — resolved via {@link Enum#valueOf(Class, String)}</li>
 * </ul>
 *
 * @author SERPRO
 */
public final class DefaultValueConverter {

    private DefaultValueConverter() {
        // utility class
    }

    /**
     * Converts the given string value to an instance of the specified target type.
     *
     * @param value      the string representation of the value
     * @param targetType the desired target type
     * @return the converted value
     * @throws IllegalArgumentException if the target type is unsupported or the
     *                                  value cannot be converted
     */
    public static Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        }
        if (targetType == char.class || targetType == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Cannot convert to char: " + value);
            }
            return value.charAt(0);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumVal = Enum.valueOf((Class<Enum>) targetType, value);
            return enumVal;
        }
        throw new IllegalArgumentException("Unsupported type for @DefaultValue: " + targetType.getName());
    }
}
