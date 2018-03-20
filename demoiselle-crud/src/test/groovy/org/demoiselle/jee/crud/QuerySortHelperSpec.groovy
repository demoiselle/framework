/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.count.QueryCountHelper
import org.demoiselle.jee.crud.entity.AddressModelForTest
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.fields.FieldsContext
import org.demoiselle.jee.crud.filter.FilterContext
import org.demoiselle.jee.crud.pagination.PaginationContext
import org.demoiselle.jee.crud.pagination.QueryPaginationHelper
import org.demoiselle.jee.crud.sort.QuerySortHelper
import org.demoiselle.jee.crud.sort.SortContext
import org.demoiselle.jee.crud.sort.SortModel
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Order
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root

/**
 * Test of {@link CrudFilter} class.
 * 
 * @author SERPRO
 */
class QuerySortHelperSpec extends Specification{
    CriteriaBuilder criteriaBuilder = Mock(CriteriaBuilder.class)
    CriteriaQuery criteriaQuery = Mock(CriteriaQuery.class)
    Root root = Mock(Root.class)
    SortContext sortContext = new SortContext(true, Collections.emptyList())
    QuerySortHelper querySortHelper = new QuerySortHelper(sortContext)

    def "QuerySortHelper shouldn't do anything if sort is disabled"() {
        given:
        sortContext.setSortEnabled(false)
        sortContext.setSorts(Arrays.asList("a", "b"))

        when:
        querySortHelper.configureOrder(criteriaBuilder, criteriaQuery, root)

        then:
        0 * criteriaQuery.orderBy(any())
    }

    def "QuerySortHelper should add ascending sorts if sort is enabled"() {
        given:
        sortContext.setSortEnabled(true)
        sortContext.setSorts(Arrays.asList(SortModel.asc("first"), SortModel.asc("second")))
        Path pathFirst = Mock(Path.class)
        Order orderFirst = Mock(Order.class)
        Path pathSecond = Mock(Path.class)
        Order orderSecond = Mock(Order.class)
        List<Order> orders = Arrays.asList(orderFirst, orderSecond)
        root.get("first") >> pathFirst
        root.get("second") >> pathSecond
        criteriaBuilder.asc(pathFirst) >> orderFirst
        criteriaBuilder.asc(pathSecond) >> orderSecond

        when:
        querySortHelper.configureOrder(criteriaBuilder, criteriaQuery, root)

        then:
        1 * criteriaQuery.orderBy(orders)
    }

    def "QuerySortHelper should add descending sorts if sort is enabled and only descending sorts are defined"() {
        given:
        sortContext.setSortEnabled(true)
        sortContext.setSorts(Arrays.asList(SortModel.desc("first"), SortModel.desc("second")))
        Path pathFirst = Mock(Path.class)
        Order orderFirst = Mock(Order.class)
        Path pathSecond = Mock(Path.class)
        Order orderSecond = Mock(Order.class)
        List<Order> orders = Arrays.asList(orderFirst, orderSecond)
        root.get("first") >> pathFirst
        root.get("second") >> pathSecond
        criteriaBuilder.desc(pathFirst) >> orderFirst
        criteriaBuilder.desc(pathSecond) >> orderSecond

        when:
        querySortHelper.configureOrder(criteriaBuilder, criteriaQuery, root)

        then:
        1 * criteriaQuery.orderBy(orders)
    }

    def "QuerySortHelper should mix ascending and descending sorts correctly"() {
        given:
        sortContext.setSortEnabled(true)
        sortContext.setSorts(Arrays.asList(SortModel.desc("first"), SortModel.asc("second")))
        Path pathFirst = Mock(Path.class)
        Order orderFirst = Mock(Order.class)
        Path pathSecond = Mock(Path.class)
        Order orderSecond = Mock(Order.class)
        List<Order> orders = Arrays.asList(orderFirst, orderSecond)
        root.get("first") >> pathFirst
        root.get("second") >> pathSecond
        criteriaBuilder.desc(pathFirst) >> orderFirst
        criteriaBuilder.asc(pathSecond) >> orderSecond

        when:
        querySortHelper.configureOrder(criteriaBuilder, criteriaQuery, root)

        then:
        1 * criteriaQuery.orderBy(orders)
    }

}
