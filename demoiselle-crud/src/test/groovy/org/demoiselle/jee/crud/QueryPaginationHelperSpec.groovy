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
import org.demoiselle.jee.crud.field.QueryFieldsHelper
import org.demoiselle.jee.crud.fields.FieldsContext
import org.demoiselle.jee.crud.filter.FilterContext
import org.demoiselle.jee.crud.pagination.PaginationContext
import org.demoiselle.jee.crud.pagination.QueryPaginationHelper
import org.demoiselle.jee.crud.pagination.ResultSet
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

/**
 * Test of {@link CrudFilter} class.
 * 
 * @author SERPRO
 */
class QueryPaginationHelperSpec extends Specification{
    
    EntityManager entityManager = Mock(EntityManager.class)
    CriteriaBuilder criteriaBuilder = Mock(CriteriaBuilder.class)
    CriteriaQuery criteriaQuery = Mock(CriteriaQuery.class)
    TypedQuery typedQuery = Mock(TypedQuery.class)
    DemoiselleCrudConfig crudConfig = Mock(DemoiselleCrudConfig.class)
    Class entityClass = UserModelForTest.class
    PaginationContext paginationContext = new PaginationContext(null, null, true)
    FilterContext filterContext = Mock(FilterContext.class)
    FieldsContext fieldsContext= Mock(FieldsContext.class)
    QueryCountHelper queryCountHelper = Mock(QueryCountHelper.class)
    QueryPaginationHelper queryPaginationHelper = new QueryPaginationHelper(entityManager, crudConfig, entityClass, paginationContext, fieldsContext, filterContext, queryCountHelper)


    def "getPaginatedResult should use the default pagination parameters if none are provided in paginationContext"() {
        given:
        def query = Mock(CriteriaQuery.class)
        entityManager.createQuery(query) >> typedQuery
        crudConfig.getDefaultPagination() >> 20
        paginationContext.setPaginationEnabled(true)
        paginationContext.setLimit(null)
        paginationContext.setOffset(null)


        def users = []
        5.times {
            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
            users << new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com", address: address)
        }
        typedQuery.getResultList() >> users
        queryCountHelper.getResultCount(query, filterContext) >> 5

        when:
        def result = queryPaginationHelper.getPaginatedResult(query)

        then:
        1 * typedQuery.setFirstResult(0)
        1 * typedQuery.setMaxResults(20)
        1 * fieldsContext.isFieldsEnabled()
        result.count == 5
        result.getPaginationContext().getLimit() == 5
    }

    def "getPaginatedResult should use given pagination parameters from paginationContext"() {
        given:
        def query = Mock(CriteriaQuery.class)
        entityManager.createQuery(query) >> typedQuery
        crudConfig.getDefaultPagination() >> 20
        paginationContext.setPaginationEnabled(true)
        paginationContext.setLimit(29)
        paginationContext.setOffset(20)

        def users = []
        20.times {
            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
            users << new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com", address: address)
        }

        typedQuery.getResultList() >> users
        queryCountHelper.getResultCount(query, filterContext) >> 50

        when:
        def result = queryPaginationHelper.getPaginatedResult(query)

        then:
        1 * typedQuery.setFirstResult(20)
        1 * typedQuery.setMaxResults(10)
        1 * fieldsContext.isFieldsEnabled()
        result.count == 50
        result.getPaginationContext().getLimit() == 29
    }

    def "It should be possible to create QueryPaginationHelper from the static method createFor"() {
        when:
        def newHelper = new QueryPaginationHelper(entityManager, crudConfig, entityClass, paginationContext, fieldsContext, filterContext, queryCountHelper)

        then:
        newHelper instanceof QueryPaginationHelper

    }
}
