/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.field.QueryFieldsHelper
import org.demoiselle.jee.crud.fields.FieldsContext
import org.demoiselle.jee.crud.mocks.MetamodelMock
import spock.lang.Specification

import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import javax.persistence.Subgraph
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.metamodel.Metamodel

/**
 * Test of {@link CrudFilter} class.
 * 
 * @author SERPRO
 */
class QueryFieldsHelperSpec extends Specification{

    EntityManager entityManager = Mock(EntityManager.class)
    CriteriaQuery criteriaQuery = Mock(CriteriaQuery.class)
    Class entityClass = UserModelForTest.class
    Metamodel metamodelMock = new MetamodelMock()
    EntityGraph graphMock = Mock(EntityGraph.class)
    TypedQuery typedQueryMock = Mock(TypedQuery.class)

    def setupMocks() {
        entityManager.getMetamodel() >> metamodelMock
        entityManager.createQuery(criteriaQuery) >> typedQueryMock
        typedQueryMock.setHint(_, _) >> typedQueryMock
    }

    def "createFilteredQuery should not do anything if fieldsEnabled is false"() {
        given:
        setupMocks()
        def fieldsContext = new FieldsContext(false, Arrays.asList("id", "name", "address.street", "address.street.country.name"), Arrays.asList("*"))
        when:
        QueryFieldsHelper.createFilteredQuery(entityManager, criteriaQuery, entityClass, fieldsContext)

        then:
        0 * entityManager.createEntityGraph(any())
    }

    def "createFilteredQuery should create a simple entity graph if fieldsEnabled is true"() {
        given:
        setupMocks()
        def fieldsContext = new FieldsContext(true, Arrays.asList("id", "name"), Arrays.asList("*"))

        when:
        def typedQuery = QueryFieldsHelper.createFilteredQuery(entityManager, criteriaQuery, entityClass, fieldsContext)

        then:
        typedQuery == typedQueryMock
        1 * entityManager.createEntityGraph(entityClass) >> graphMock
        1 * graphMock.addAttributeNodes("id")
        1 * graphMock.addAttributeNodes("name")

        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
    }

    def "createFilteredQuery should ignore non-entity fields"() {
        given:
        setupMocks()
        def fieldsContext = new FieldsContext(true, Arrays.asList("id", "name", "ignore"), Arrays.asList("*"))

        when:
        def typedQuery = QueryFieldsHelper.createFilteredQuery(entityManager, criteriaQuery, entityClass, fieldsContext)

        then:
        typedQuery == typedQueryMock
        1 * entityManager.createEntityGraph(entityClass) >> graphMock
        1 * graphMock.addAttributeNodes("id")
        1 * graphMock.addAttributeNodes("name")

        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
    }

    def "createFilteredQuery should create a subgraphs if fieldsEnabled is true and subfields are enabled"() {
        given:
        setupMocks()
        def fieldsContext = new FieldsContext(true, Arrays.asList("id", "name", "address.street", "address.country.name"), Arrays.asList("*"))

        def subgraphCountryMock = Mock(Subgraph.class)
        def subgraphAddressMock = Mock(Subgraph.class)
        when:
        def typedQuery = QueryFieldsHelper.createFilteredQuery(entityManager, criteriaQuery, entityClass, fieldsContext)

        then:
        typedQuery == typedQueryMock
        1 * entityManager.createEntityGraph(entityClass) >> graphMock
        1 * graphMock.addAttributeNodes("id")
        1 * graphMock.addAttributeNodes("name")
        1 * graphMock.addSubgraph("address") >> subgraphAddressMock
        1 * subgraphAddressMock.addAttributeNodes("street")
        1 * subgraphAddressMock.addSubgraph("country") >> subgraphCountryMock
        1 * subgraphCountryMock.addAttributeNodes("name")

        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
        1 * typedQueryMock.setHint("javax.persistence.loadgraph", graphMock) >> typedQueryMock
    }


}
