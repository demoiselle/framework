/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configuration holder for CRUD request limits. Guards the framework against
 * abusive or accidental heavy queries by bounding the number of filters, the
 * total amount of filter values, the length of an individual filter value and
 * the number of sort fields accepted per request.
 *
 * <p>
 * All getters are null-safe: whenever a configured value is {@code null} or a
 * non-positive number the corresponding built-in default is returned, so
 * callers always observe a usable, positive limit.
 * </p>
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.crud.limits")
public class CrudLimitsConfig {

    private static final int DEFAULT_MAX_FILTERS = 20;
    private static final int DEFAULT_MAX_FILTER_VALUES = 100;
    private static final int DEFAULT_MAX_FILTER_VALUE_LENGTH = 1024;
    private static final int DEFAULT_MAX_SORT_FIELDS = 10;

    private Integer maxFilters = DEFAULT_MAX_FILTERS;
    private Integer maxFilterValues = DEFAULT_MAX_FILTER_VALUES;
    private Integer maxFilterValueLength = DEFAULT_MAX_FILTER_VALUE_LENGTH;
    private Integer maxSortFields = DEFAULT_MAX_SORT_FIELDS;

    /**
     * @return the maximum number of distinct filters allowed per request
     */
    public int getMaxFilters() {
        return safe(maxFilters, DEFAULT_MAX_FILTERS);
    }

    /**
     * @return the maximum total number of filter values allowed per request
     */
    public int getMaxFilterValues() {
        return safe(maxFilterValues, DEFAULT_MAX_FILTER_VALUES);
    }

    /**
     * @return the maximum length of an individual filter value
     */
    public int getMaxFilterValueLength() {
        return safe(maxFilterValueLength, DEFAULT_MAX_FILTER_VALUE_LENGTH);
    }

    /**
     * @return the maximum number of sort fields allowed per request
     */
    public int getMaxSortFields() {
        return safe(maxSortFields, DEFAULT_MAX_SORT_FIELDS);
    }

    private static int safe(Integer value, int defaultValue) {
        return (value == null || value <= 0) ? defaultValue : value;
    }
}
