package org.demoiselle.jee.crud.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Interface funcional que encapsula um predicado JPA reutilizável.
 * Permite composição declarativa de consultas complexas via {@link #and}, {@link #or} e {@link #not}.
 *
 * @param <T> o tipo da entidade raiz
 */
@FunctionalInterface
public interface Specification<T> {

    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    default Specification<T> and(Specification<T> other) {
        return (root, query, cb) -> cb.and(
            this.toPredicate(root, query, cb),
            other.toPredicate(root, query, cb)
        );
    }

    default Specification<T> or(Specification<T> other) {
        return (root, query, cb) -> cb.or(
            this.toPredicate(root, query, cb),
            other.toPredicate(root, query, cb)
        );
    }

    default Specification<T> not() {
        return (root, query, cb) -> cb.not(this.toPredicate(root, query, cb));
    }
}
