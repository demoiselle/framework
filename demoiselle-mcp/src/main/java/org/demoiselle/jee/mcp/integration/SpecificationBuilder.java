/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds filter predicates from JSON arguments for MCP tool queries.
 *
 * <p>This class is registered conditionally when {@code demoiselle-crud} is on
 * the classpath. It converts JSON filter arguments (field, operator, value)
 * into a list of validated filter descriptors that can be used to construct
 * JPA {@code Specification} or {@code Predicate} objects.</p>
 *
 * <p>Since the actual JPA {@code Specification} interface is part of the optional
 * {@code demoiselle-crud} module, this builder works with a {@code Map}-based
 * representation of filters, avoiding a hard compile-time dependency.</p>
 *
 * <p>Supported operators:</p>
 * <ul>
 *   <li>{@code eq} — equals</li>
 *   <li>{@code ne} — not equals</li>
 *   <li>{@code gt} — greater than</li>
 *   <li>{@code ge} — greater than or equal</li>
 *   <li>{@code lt} — less than</li>
 *   <li>{@code le} — less than or equal</li>
 *   <li>{@code like} — SQL LIKE pattern</li>
 *   <li>{@code in} — value in collection</li>
 *   <li>{@code isNull} — field is null</li>
 * </ul>
 *
 * @param <T> the entity type being filtered
 */
public class SpecificationBuilder<T> {

    /**
     * Supported filter operators.
     */
    public enum Operator {
        EQ, NE, GT, GE, LT, LE, LIKE, IN, IS_NULL;

        /**
         * Parses an operator string (case-insensitive).
         *
         * @param value the operator string
         * @return the corresponding {@code Operator}
         * @throws IllegalArgumentException if the operator is not recognized
         */
        public static Operator fromString(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Operator must not be null or blank");
            }
            String normalized = value.trim().toUpperCase()
                    .replace(" ", "_");
            // Handle camelCase "isNull" → "IS_NULL"
            if ("ISNULL".equals(normalized)) {
                return IS_NULL;
            }
            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Unsupported operator: '" + value + "'. Supported: "
                                + Set.of(values()));
            }
        }
    }

    /**
     * A validated filter descriptor containing field name, operator, and value.
     */
    public record FilterDescriptor(
            String field,
            Operator operator,
            Object value
    ) {}

    /**
     * Builds a list of validated filter descriptors from JSON filter arguments.
     *
     * <p>Each filter in the input list must be a map with keys:</p>
     * <ul>
     *   <li>{@code field} — the entity field name (required)</li>
     *   <li>{@code operator} — the filter operator (required)</li>
     *   <li>{@code value} — the filter value (optional for {@code isNull})</li>
     * </ul>
     *
     * @param entityClass the target entity class for field validation
     * @param filters     the list of filter maps from JSON arguments
     * @return a list of validated {@link FilterDescriptor} objects
     * @throws IllegalArgumentException if a field does not exist on the entity
     *                                  or an operator is not supported
     * @throws NullPointerException     if {@code entityClass} or {@code filters} is null
     */
    public List<FilterDescriptor> build(Class<T> entityClass, List<Map<String, Object>> filters) {
        if (entityClass == null) {
            throw new NullPointerException("entityClass must not be null");
        }
        if (filters == null) {
            throw new NullPointerException("filters must not be null");
        }

        Set<String> validFields = collectFieldNames(entityClass);
        List<FilterDescriptor> result = new ArrayList<>();

        for (Map<String, Object> filter : filters) {
            String field = (String) filter.get("field");
            String operatorStr = (String) filter.get("operator");
            Object value = filter.get("value");

            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("Filter field must not be null or blank");
            }
            if (!validFields.contains(field)) {
                throw new IllegalArgumentException(
                        "Field '" + field + "' does not exist on entity "
                                + entityClass.getSimpleName()
                                + ". Valid fields: " + validFields);
            }

            Operator operator = Operator.fromString(operatorStr);

            // Validate value presence for non-isNull operators
            if (operator != Operator.IS_NULL && value == null) {
                throw new IllegalArgumentException(
                        "Value is required for operator '" + operator + "' on field '" + field + "'");
            }

            // Validate IN operator expects a collection
            if (operator == Operator.IN && !(value instanceof Collection)) {
                throw new IllegalArgumentException(
                        "Operator 'IN' requires a collection value for field '" + field + "'");
            }

            result.add(new FilterDescriptor(field, operator, value));
        }

        return result;
    }

    /**
     * Applies a list of filter descriptors to a target map, returning a new map
     * containing only entries that match all filters (AND logic).
     *
     * <p>This method provides a simple in-memory filtering mechanism for testing
     * and non-JPA use cases.</p>
     *
     * @param record  the record to test as a map of field→value
     * @param filters the filter descriptors to apply
     * @return {@code true} if the record matches all filters
     */
    public boolean matches(Map<String, Object> record, List<FilterDescriptor> filters) {
        for (FilterDescriptor filter : filters) {
            if (!matchesSingle(record.get(filter.field()), filter)) {
                return false;
            }
        }
        return true;
    }

    // ── Internal helpers ──

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchesSingle(Object fieldValue, FilterDescriptor filter) {
        switch (filter.operator()) {
            case IS_NULL:
                return fieldValue == null;
            case EQ:
                return filter.value().equals(fieldValue);
            case NE:
                return !filter.value().equals(fieldValue);
            case GT:
                return fieldValue instanceof Comparable
                        && ((Comparable) fieldValue).compareTo(filter.value()) > 0;
            case GE:
                return fieldValue instanceof Comparable
                        && ((Comparable) fieldValue).compareTo(filter.value()) >= 0;
            case LT:
                return fieldValue instanceof Comparable
                        && ((Comparable) fieldValue).compareTo(filter.value()) < 0;
            case LE:
                return fieldValue instanceof Comparable
                        && ((Comparable) fieldValue).compareTo(filter.value()) <= 0;
            case LIKE:
                if (fieldValue == null || filter.value() == null) return false;
                String pattern = filter.value().toString()
                        .replace("%", ".*")
                        .replace("_", ".");
                return fieldValue.toString().matches(pattern);
            case IN:
                return filter.value() instanceof Collection
                        && ((Collection<?>) filter.value()).contains(fieldValue);
            default:
                return false;
        }
    }

    private Set<String> collectFieldNames(Class<?> clazz) {
        Map<String, Boolean> fields = new LinkedHashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                fields.putIfAbsent(f.getName(), Boolean.TRUE);
            }
            current = current.getSuperclass();
        }
        return fields.keySet();
    }
}
