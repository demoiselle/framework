/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.count.QueryCountHelper
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.field.QueryFieldsHelper
import org.demoiselle.jee.crud.fields.FieldsContext
import org.demoiselle.jee.crud.filter.FilterContext
import org.demoiselle.jee.crud.helper.DemoiselleCrudHelper
import org.demoiselle.jee.crud.pagination.PaginationContext
import org.demoiselle.jee.crud.pagination.QueryPaginationHelper
import org.demoiselle.jee.crud.sort.QuerySortHelper
import org.demoiselle.jee.crud.sort.SortContext
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * Test of {@link CrudFilter} class.
 * 
 * @author SERPRO
 */
class DemoiselleCrudHelperSpec extends Specification{
    
    EntityManager entityManager = Mock(EntityManager.class)
    DemoiselleCrudConfig crudConfig = Mock(DemoiselleCrudConfig.class)
    Class entityClass = UserModelForTest.class
    PaginationContext paginationContext = new PaginationContext(null, null, true)
    FilterContext filterContext = Mock(FilterContext.class)
    FieldsContext fieldsContext= Mock(FieldsContext.class)
    QueryCountHelper queryCountHelper = Mock(QueryCountHelper.class)
    QueryPaginationHelper queryPaginationHelper = Mock(QueryPaginationHelper.class)
    QueryFieldsHelper queryFieldsHelper = Mock(QueryFieldsHelper.class)
    QuerySortHelper querySortHelper = Mock(QuerySortHelper.class)
    CriteriaQuery criteriaQuery = Mock(CriteriaQuery.class)
    CrudMessage crudMessage = Mock(CrudMessage.class)
    Root root = Mock(Root.class)
    TypedQuery queryMock = Mock(TypedQuery.class)
    List listMock = []

    def startup() {
        crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(_) >> "fieldRequestDoesNotExistsOnDemoiselleResultField"
        crudMessage.fieldRequestDoesNotExistsOnObject(_, _) >> "fieldRequestDoesNotExistsOnObject"
        entityManager.createQuery(criteriaQuery) >> queryMock
        queryMock.getResultList() >> listMock
    }

    def "fields not present on allowedFields should not be allowed when fieldsContext is true"() {
        startup()

        given:
        def crudBuilder = new DemoiselleCrudHelper.Builder(false, entityManager, entityClass)
                .setResultClass(entityClass)
                .setCrudMessage(crudMessage)
                .setDemoiselleRequestContext(new DemoiselleRequestContextImpl())
                .setFieldsContext(new FieldsContext(true, Arrays.asList("id", "name", "address.*"), Arrays.asList("id", "name") ))
                .setPaginationContext(PaginationContext.disabledPagination())
                .setSortContext(SortContext.disabledSort())
                .setFilterContext(FilterContext.disabledFilter());

        when:
        crudBuilder
            .build()
            .executeQuery(criteriaQuery, root)

        then:
        thrown(IllegalArgumentException.class)
        1 * crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(_)


        when:
        crudBuilder
            .setFieldsContext(new FieldsContext(false, Arrays.asList("id", "name", "address.*"), Arrays.asList("id", "name") ))
            .build()
            .executeQuery(criteriaQuery, root)

        then:
        notThrown(IllegalArgumentException.class)

    }

}
