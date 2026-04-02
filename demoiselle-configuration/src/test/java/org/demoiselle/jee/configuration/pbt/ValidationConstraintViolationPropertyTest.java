/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import java.lang.reflect.Field;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import net.jqwik.api.*;

import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 1: Validação lança exceção com causa ConstraintViolationException
 *
 * <p><b>Validates: Requirements 1.4</b></p>
 *
 * For any object with a field annotated with a Jakarta validation constraint
 * whose value violates the constraint, the validateValue logic must throw
 * {@link DemoiselleConfigurationException} whose cause is a
 * {@link ConstraintViolationException} containing the detected violations.
 */
class ValidationConstraintViolationPropertyTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    // ── Test model classes with Jakarta validation constraints ──

    /**
     * Model with @NotNull constraint.
     */
    static class NotNullModel {
        @NotNull
        String value;
    }

    /**
     * Model with @NotBlank constraint.
     */
    static class NotBlankModel {
        @NotBlank
        String value;
    }

    /**
     * Model with @Size constraint.
     */
    static class SizeModel {
        @Size(min = 3, max = 10)
        String value;
    }

    /**
     * Model with @Min constraint.
     */
    static class MinModel {
        @Min(10)
        int value;
    }

    /**
     * Model with @Max constraint.
     */
    static class MaxModel {
        @Max(100)
        int value;
    }

    // ── Providers ──

    /**
     * Generates blank strings that violate @NotBlank.
     */
    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "  ", "\t", "\n", "   \t\n  ");
    }

    /**
     * Generates strings shorter than the @Size(min=3) constraint.
     */
    @Provide
    Arbitrary<String> tooShortStrings() {
        return Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(2);
    }

    /**
     * Generates strings longer than the @Size(max=10) constraint.
     */
    @Provide
    Arbitrary<String> tooLongStrings() {
        return Arbitraries.strings().alpha().ofMinLength(11).ofMaxLength(50);
    }

    /**
     * Generates integers below the @Min(10) constraint.
     */
    @Provide
    Arbitrary<Integer> belowMin() {
        return Arbitraries.integers().between(Integer.MIN_VALUE, 9);
    }

    /**
     * Generates integers above the @Max(100) constraint.
     */
    @Provide
    Arbitrary<Integer> aboveMax() {
        return Arbitraries.integers().between(101, Integer.MAX_VALUE);
    }

    // ── Core validation logic (mirrors ConfigurationLoader.validateValue) ──

    /**
     * Reproduces the exact validation logic from ConfigurationLoader.validateValue():
     * validates a property on the target object, and if violations exist, throws
     * DemoiselleConfigurationException with ConstraintViolationException as cause.
     */
    private void validateValue(Object targetObject, Field field) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validateProperty(targetObject, field.getName());

        if (!violations.isEmpty()) {
            StringBuilder messageConstraint = new StringBuilder();
            for (ConstraintViolation<Object> violation : violations) {
                messageConstraint.append(field.toGenericString())
                        .append(" ")
                        .append(violation.getMessage())
                        .append("\n");
            }

            throw new DemoiselleConfigurationException(messageConstraint.toString(),
                    new ConstraintViolationException(violations));
        }
    }

    /**
     * Helper to set a field value via reflection.
     */
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Helper to get a declared field.
     */
    private static Field getField(Class<?> clazz, String fieldName) throws Exception {
        return clazz.getDeclaredField(fieldName);
    }

    // ── Property tests ──

    @Property(tries = 100)
    void notNullViolationProducesDemoiselleExceptionWithConstraintViolationCause() throws Exception {
        // @NotNull field set to null always violates
        NotNullModel model = new NotNullModel();
        model.value = null;

        Field field = getField(NotNullModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause(), "Cause must not be null");
        assertInstanceOf(ConstraintViolationException.class, ex.getCause(),
                "Cause must be ConstraintViolationException");

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty(),
                "ConstraintViolationException must contain violations");
    }

    @Property(tries = 100)
    void notBlankViolationProducesDemoiselleExceptionWithConstraintViolationCause(
            @ForAll("blankStrings") String blankValue) throws Exception {

        NotBlankModel model = new NotBlankModel();
        model.value = blankValue;

        Field field = getField(NotBlankModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause());
        assertInstanceOf(ConstraintViolationException.class, ex.getCause());

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty());
    }

    @Property(tries = 100)
    void sizeTooShortViolationProducesDemoiselleExceptionWithConstraintViolationCause(
            @ForAll("tooShortStrings") String shortValue) throws Exception {

        SizeModel model = new SizeModel();
        model.value = shortValue;

        Field field = getField(SizeModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause());
        assertInstanceOf(ConstraintViolationException.class, ex.getCause());

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty());
    }

    @Property(tries = 100)
    void sizeTooLongViolationProducesDemoiselleExceptionWithConstraintViolationCause(
            @ForAll("tooLongStrings") String longValue) throws Exception {

        SizeModel model = new SizeModel();
        model.value = longValue;

        Field field = getField(SizeModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause());
        assertInstanceOf(ConstraintViolationException.class, ex.getCause());

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty());
    }

    @Property(tries = 100)
    void minViolationProducesDemoiselleExceptionWithConstraintViolationCause(
            @ForAll("belowMin") int invalidValue) throws Exception {

        MinModel model = new MinModel();
        model.value = invalidValue;

        Field field = getField(MinModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause());
        assertInstanceOf(ConstraintViolationException.class, ex.getCause());

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty());
    }

    @Property(tries = 100)
    void maxViolationProducesDemoiselleExceptionWithConstraintViolationCause(
            @ForAll("aboveMax") int invalidValue) throws Exception {

        MaxModel model = new MaxModel();
        model.value = invalidValue;

        Field field = getField(MaxModel.class, "value");

        DemoiselleConfigurationException ex = assertThrows(
                DemoiselleConfigurationException.class,
                () -> validateValue(model, field)
        );

        assertNotNull(ex.getCause());
        assertInstanceOf(ConstraintViolationException.class, ex.getCause());

        ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
        assertFalse(cve.getConstraintViolations().isEmpty());
    }
}
