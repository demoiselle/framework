/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import com.google.common.collect.Sets
import org.demoiselle.jee.crud.entity.UserType
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.filter.FilterContext
import org.demoiselle.jee.crud.filter.QueryPredicatesHelper
import spock.lang.Specification

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * Test of {@link CrudFilter} class.
 *
 * @author SERPRO
 */
class QueryPredicatesHelperSpec extends Specification{

    FilterContext filterContext = new FilterContext(true, null, null)
    Class<?> entityClass = UserModelForTest.class
    CriteriaBuilder criteriaBuilder = Mock(CriteriaBuilder.class)
    CriteriaQuery criteriaQuery = Mock(CriteriaQuery.class)
    QueryPredicatesHelper queryPredicatesHelper = new QueryPredicatesHelper(entityClass, filterContext)


    def "buildPredicates should add AND predicates for different predicate keys"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("id", Sets.newHashSet("1234"))
        filters.addChild("name", Sets.newHashSet("nameVal"))
        filterContext.setFilters(filters)
        Predicate equalIdPred = Mock(Predicate.class)
        Predicate equalNamePred = Mock(Predicate.class)
        Predicate andIdPred = Mock(Predicate.class)
        Predicate andNamePred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path idPath = Mock(Path.class)
        Path namePath = Mock(Path.class)
        root.get("id") >> idPath
        root.get("name") >> namePath
        criteriaBuilder.equal(idPath, "1234") >> equalIdPred
        criteriaBuilder.equal(namePath, "nameVal") >> equalNamePred
        criteriaBuilder.and([ equalIdPred ]) >> andIdPred
        criteriaBuilder.and([ equalNamePred ]) >> andNamePred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andIdPred
        predicates[1] == andNamePred
        predicates.length == 2
    }

    def "buildPredicates should add OR predicates for more than one entry of the same predicate key"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        def idChild = filters.addChild("joined", Collections.emptySet())
        idChild.addChild("id", Sets.newHashSet("1234", "5678"))
        filterContext.setFilters(filters)
        Predicate equalId1Pred = Mock(Predicate.class)
        Predicate equalId2Pred = Mock(Predicate.class)
        Predicate orIdPred = Mock(Predicate.class)

        Root root = Mock(Root.class)
        Join joined = Mock(Join.class)
        Path idPath = Mock(Path.class)
        root.join("joined") >> joined
        joined.get("id") >> idPath
//        (criteriaBuilder.equal(join.get(values.getKey()), value)
        criteriaBuilder.equal(idPath, "1234") >> equalId1Pred
        criteriaBuilder.equal(idPath, "5678") >> equalId2Pred
        criteriaBuilder.or([ equalId1Pred, equalId2Pred ]) >> orIdPred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == orIdPred
        predicates.length == 1
    }

    def "buildPredicates should add isNull predicates when the value for the parameter is 'null'"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("id", Sets.newHashSet("null"))
        filterContext.setFilters(filters)
        Predicate isNullPred = Mock(Predicate.class)
        Predicate andIsNullPred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path idPath = Mock(Path.class)
        root.get("id") >> idPath
        criteriaBuilder.isNull(idPath) >> isNullPred
        criteriaBuilder.and([isNullPred]) >> andIsNullPred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andIsNullPred
        predicates.length == 1
    }

    /*
                            child.getValue().stream().forEach(value -> {
                            } else if (isLikeFilter(child.getKey(), value)) {
                                predicateAndKeys.add(buildLikePredicate(criteriaBuilder, criteriaQuery, root, child.getKey(), value));
                            } else if (value.equalsIgnoreCase("isTrue")) {
                                predicateAndKeys.add(criteriaBuilder.isTrue(root.get(child.getKey())));
                            } else if (value.equalsIgnoreCase("isFalse")) {
                                predicateAndKeys.add(criteriaBuilder.isFalse(root.get(child.getKey())));
                            } else if (isEnumFilter(child.getKey(), value)) {
                                predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertEnumToInt(child.getKey(), value)));
     */
    def "buildPredicates should add isEmpty predicates when the value for the parameter is an empty string"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("id", Sets.newHashSet("null"))
        filterContext.setFilters(filters)
        Predicate isNullPred = Mock(Predicate.class)
        Predicate andIsNullPred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path idPath = Mock(Path.class)
        root.get("id") >> idPath
        criteriaBuilder.isNull(idPath) >> isNullPred
        criteriaBuilder.and([isNullPred]) >> andIsNullPred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andIsNullPred
        predicates.length == 1
    }

    def "buildPredicates should add a LIKE filter if a value parameter starts with or ends with *"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("name", Sets.newHashSet("*VALUE", "VALUE*"))
        filterContext.setFilters(filters)
        Predicate likeStartPred = Mock(Predicate.class)
        Predicate likeEndPred = Mock(Predicate.class)
        Predicate andLikePred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path namePath = Mock(Path.class)
        Path lowerPath = Mock(Path.class)
        root.get("name") >> namePath
        criteriaBuilder.lower(namePath) >> lowerPath

        criteriaBuilder.like(lowerPath, "%value") >> likeStartPred
        criteriaBuilder.like(lowerPath, "value%") >> likeEndPred
        criteriaBuilder.and([likeStartPred, likeEndPred]) >> andLikePred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andLikePred
        predicates.length == 1
    }

    def "buildPredicates should add isTrue and isFalse filters if values correspond to the strings 'isTrue' or 'isFalse' respectively"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("name", Sets.newHashSet("isTrue", "isFalse"))
        filterContext.setFilters(filters)
        Predicate isTruePred = Mock(Predicate.class)
        Predicate isFalsePred = Mock(Predicate.class)
        Predicate andBooleanPred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path namePath = Mock(Path.class)
        Path lowerPath = Mock(Path.class)
        root.get("name") >> namePath

        criteriaBuilder.isTrue(namePath) >> isTruePred
        criteriaBuilder.isFalse(namePath) >> isFalsePred
        criteriaBuilder.and([isTruePred, isFalsePred]) >> andBooleanPred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andBooleanPred
        predicates.length == 1
    }

    def "buildPredicates should add equal filters to the ordinal values when the field is an enum"() {
        given:
        TreeNodeField<String, Set<String>> filters = new TreeNodeField<String, Set<String>>(UserModelForTest.class.getName(), Collections.emptySet())

        filters.addChild("userType", Sets.newHashSet("MANAGER"))
        filterContext.setFilters(filters)
        Predicate equalEnumPred = Mock(Predicate.class)
        Predicate andEnumPred = Mock(Predicate.class)
        Root root = Mock(Root.class)
        Path namePath = Mock(Path.class)
        root.get("userType") >> namePath

        criteriaBuilder.equal(namePath, UserType.MANAGER.ordinal()) >> equalEnumPred
        criteriaBuilder.and([ equalEnumPred ]) >> andEnumPred

        when:
        def predicates = queryPredicatesHelper.buildPredicates(criteriaBuilder, criteriaQuery, root)

        then:
        predicates[0] == andEnumPred
        predicates.length == 1
    }



}