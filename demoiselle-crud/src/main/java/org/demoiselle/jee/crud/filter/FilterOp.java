package org.demoiselle.jee.crud.filter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hierarquia selada de operações de filtro para consultas CRUD.
 * Cada variante representa uma operação específica de predicado JPA.
 */
public sealed interface FilterOp {

    String key();

    record Equals(String key, String value) implements FilterOp {
        public Equals {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record Like(String key, String pattern) implements FilterOp {
        public Like {
            Objects.requireNonNull(key);
            Objects.requireNonNull(pattern);
        }
    }

    record IsNull(String key) implements FilterOp {
        public IsNull {
            Objects.requireNonNull(key);
        }
    }

    record IsTrue(String key) implements FilterOp {
        public IsTrue {
            Objects.requireNonNull(key);
        }
    }

    record IsFalse(String key) implements FilterOp {
        public IsFalse {
            Objects.requireNonNull(key);
        }
    }

    record EnumFilter(String key, String value, int ordinal) implements FilterOp {
        public EnumFilter {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            if (ordinal < 0) {
                throw new IllegalArgumentException("ordinal must be >= 0");
            }
        }
    }

    record UUIDFilter(String key, UUID value) implements FilterOp {
        public UUIDFilter {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record GreaterThan(String key, String value) implements FilterOp {
        public GreaterThan {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record LessThan(String key, String value) implements FilterOp {
        public LessThan {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record GreaterThanOrEqual(String key, String value) implements FilterOp {
        public GreaterThanOrEqual {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record LessThanOrEqual(String key, String value) implements FilterOp {
        public LessThanOrEqual {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    record Between(String key, String lower, String upper) implements FilterOp {
        public Between {
            Objects.requireNonNull(key);
            Objects.requireNonNull(lower);
            Objects.requireNonNull(upper);
        }
    }

    record In(String key, List<String> values) implements FilterOp {
        public In {
            Objects.requireNonNull(key);
            Objects.requireNonNull(values);
            values = List.copyOf(values);
        }
    }
}
