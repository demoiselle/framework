package org.demoiselle.jee.crud.sort;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortContext;
import org.demoiselle.jee.crud.sort.SortModel;

/**
 *
 *
 */
public class QuerySortHelper {
    private SortContext sortContext;

    public QuerySortHelper(SortContext sortContext) {
        this.sortContext = sortContext;
    }

    public <T> void configureOrder(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery, Root<T> root) {
        if (sortContext.isSortEnabled() && !sortContext.getSorts().isEmpty()) {
            List<Order> orders = new ArrayList<>();

            sortContext.getSorts().forEach(sortModel -> {

                if (sortModel.getType().equals(CrudSort.ASC)) {
                    orders.add(criteriaBuilder.asc(root.get(sortModel.getField())));
                } else {
                    orders.add(criteriaBuilder.desc(root.get(sortModel.getField())));
                }
            });
            criteriaQuery.orderBy(orders);
        }

    }
}
