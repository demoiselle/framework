/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultValueConverter}.
 *
 * <p>Validates: Requirements 4.2, 4.5, 4.6, 4.7</p>
 *
 * Verifies conversion for each supported type with specific examples
 * and edge cases such as empty strings, whitespace, invalid enum names,
 * and unsupported types.
 */
class DefaultValueConverterTest {

    // ── String ──

    @Nested
    @DisplayName("String conversion")
    class StringConversion {

        @Test
        @DisplayName("should return value as-is")
        void returnsValueAsIs() {
            assertEquals("hello", DefaultValueConverter.convert("hello", String.class));
        }

        @Test
        @DisplayName("should return empty string as-is")
        void returnsEmptyString() {
            assertEquals("", DefaultValueConverter.convert("", String.class));
        }
    }

    // ── int / Integer ──

    @Nested
    @DisplayName("int/Integer conversion")
    class IntConversion {

        @Test
        @DisplayName("\"42\" → 42")
        void convertsPositive() {
            assertEquals(42, DefaultValueConverter.convert("42", int.class));
            assertEquals(42, DefaultValueConverter.convert("42", Integer.class));
        }

        @Test
        @DisplayName("\"-1\" → -1")
        void convertsNegative() {
            assertEquals(-1, DefaultValueConverter.convert("-1", int.class));
            assertEquals(-1, DefaultValueConverter.convert("-1", Integer.class));
        }
    }


    // ── long / Long ──

    @Nested
    @DisplayName("long/Long conversion")
    class LongConversion {

        @Test
        @DisplayName("\"123456789\" → 123456789L")
        void convertsLong() {
            assertEquals(123456789L, DefaultValueConverter.convert("123456789", long.class));
            assertEquals(123456789L, DefaultValueConverter.convert("123456789", Long.class));
        }
    }

    // ── boolean / Boolean ──

    @Nested
    @DisplayName("boolean/Boolean conversion")
    class BooleanConversion {

        @Test
        @DisplayName("\"true\" → true")
        void convertsTrue() {
            assertEquals(true, DefaultValueConverter.convert("true", boolean.class));
            assertEquals(true, DefaultValueConverter.convert("true", Boolean.class));
        }

        @Test
        @DisplayName("\"false\" → false")
        void convertsFalse() {
            assertEquals(false, DefaultValueConverter.convert("false", boolean.class));
            assertEquals(false, DefaultValueConverter.convert("false", Boolean.class));
        }

        @Test
        @DisplayName("\"anything\" → false (Boolean.parseBoolean behavior)")
        void nonBooleanStringReturnsFalse() {
            assertEquals(false, DefaultValueConverter.convert("anything", boolean.class));
            assertEquals(false, DefaultValueConverter.convert("anything", Boolean.class));
        }
    }

    // ── double / Double ──

    @Nested
    @DisplayName("double/Double conversion")
    class DoubleConversion {

        @Test
        @DisplayName("\"3.14\" → 3.14")
        void convertsDouble() {
            assertEquals(3.14, DefaultValueConverter.convert("3.14", double.class));
            assertEquals(3.14, DefaultValueConverter.convert("3.14", Double.class));
        }
    }

    // ── float / Float ──

    @Nested
    @DisplayName("float/Float conversion")
    class FloatConversion {

        @Test
        @DisplayName("\"2.5\" → 2.5f")
        void convertsFloat() {
            assertEquals(2.5f, DefaultValueConverter.convert("2.5", float.class));
            assertEquals(2.5f, DefaultValueConverter.convert("2.5", Float.class));
        }
    }

    // ── short / Short ──

    @Nested
    @DisplayName("short/Short conversion")
    class ShortConversion {

        @Test
        @DisplayName("\"100\" → (short)100")
        void convertsShort() {
            assertEquals((short) 100, DefaultValueConverter.convert("100", short.class));
            assertEquals((short) 100, DefaultValueConverter.convert("100", Short.class));
        }
    }

    // ── byte / Byte ──

    @Nested
    @DisplayName("byte/Byte conversion")
    class ByteConversion {

        @Test
        @DisplayName("\"127\" → (byte)127")
        void convertsByte() {
            assertEquals((byte) 127, DefaultValueConverter.convert("127", byte.class));
            assertEquals((byte) 127, DefaultValueConverter.convert("127", Byte.class));
        }
    }

    // ── char / Character ──

    @Nested
    @DisplayName("char/Character conversion")
    class CharConversion {

        @Test
        @DisplayName("\"A\" → 'A'")
        void convertsChar() {
            assertEquals('A', DefaultValueConverter.convert("A", char.class));
            assertEquals('A', DefaultValueConverter.convert("A", Character.class));
        }
    }

    // ── Enum ──

    @Nested
    @DisplayName("Enum conversion")
    class EnumConversion {

        @Test
        @DisplayName("\"PROPERTIES\" → ConfigurationType.PROPERTIES")
        void convertsEnum() {
            assertEquals(ConfigurationType.PROPERTIES,
                    DefaultValueConverter.convert("PROPERTIES", ConfigurationType.class));
        }
    }

    // ── Edge cases ──

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty string for int throws NumberFormatException")
        void emptyStringForIntThrows() {
            assertThrows(NumberFormatException.class,
                    () -> DefaultValueConverter.convert("", int.class));
        }

        @Test
        @DisplayName("whitespace string for int throws NumberFormatException")
        void whitespaceStringForIntThrows() {
            assertThrows(NumberFormatException.class,
                    () -> DefaultValueConverter.convert("  ", int.class));
        }

        @Test
        @DisplayName("invalid enum name throws IllegalArgumentException")
        void invalidEnumNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> DefaultValueConverter.convert("INVALID", ConfigurationType.class));
        }

        @Test
        @DisplayName("empty string for char throws IllegalArgumentException")
        void emptyStringForCharThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> DefaultValueConverter.convert("", char.class));
        }

        @Test
        @DisplayName("multi-char string for char throws IllegalArgumentException")
        void multiCharStringForCharThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> DefaultValueConverter.convert("ab", char.class));
        }

        @Test
        @DisplayName("unsupported type (List.class) throws IllegalArgumentException")
        void unsupportedTypeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> DefaultValueConverter.convert("value", List.class));
        }
    }
}
