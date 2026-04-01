package org.demoiselle.jee.crud.filter;

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
}
