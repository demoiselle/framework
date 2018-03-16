/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.pagination.PaginationContext
import org.demoiselle.jee.crud.pagination.PaginationHelper
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage
import org.demoiselle.jee.crud.pagination.ResultSet
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.BadRequestException
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

/**
 * 
 * Test of {@link PaginationHelper} class.
 * 
 * @author SERPRO

 */
class PaginationHelperSpec extends Specification {
    
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    
    ResourceInfo resourceInfo = Mock()
    UriInfo uriInfo = Mock()
    PaginationHelperMessage message = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoiselleCrudConfig crudConfig = Mock()

    PaginationHelper paginationHelper = new PaginationHelper(resourceInfo, uriInfo, crudConfig,  drc, message)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        configureRequestForCrud()
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        drc.paginationContext = new PaginationContext(20, 10, true)
        mvmRequest.putSingle("range", "10-20") 
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.paginationContext.offset == 10
        drc.paginationContext.limit == 20
        notThrown(BadRequestException)
        
    }
    
    @Unroll
    def "A request with invalid 'range' parameters (#offset, #limit) should throw RuntimeException"(offset, limit) {
        given:
        configureRequestForCrud()
        String parameter = "${offset}-${limit}".toString()
        
        crudConfig.getDefaultPagination() >> 10
        crudConfig.isPaginationEnabled() >> true
        mvmRequest.putSingle("range", parameter) 
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(RuntimeException)
         
        where:
        // Offset and limit should be a number
        offset  | limit
        1       | 0
        -1      | 0
        "a"     | 10
        10      | "a"
        3       | ""
        ""      | ""
        "a"     | "-"
        -3      | -2
        -3      | -4
        -3      | -3
        0       | 10 // Greater than default pagination
        
    }

    @Unroll
    def "A partial response should build a 'Link' header with range: [defaultPagination: #defaultPagination, range: #range, limit: #limit, offset: #offset]"(defaultPagination, range, limit, offset){
        
        given:
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        crudConfig.getDefaultPagination() >> defaultPagination
        crudConfig.isPaginationEnabled() >> true
        
        def contentMock = 1..100
        drc.paginationContext = new PaginationContext(limit, offset, true)
        result.paginationContext = drc.paginationContext
        result.entityClass = UserRestForTest.class
        result.content = contentMock //.subList(drc.paginationContext.offset, drc.paginationContext.limit + 1)
        result.count = result.content.size()
        String queryParamString = "?date=2017-01-01&mail=test@test.com,test2@test.com"
        String url = "http://localhost:9090/api/users${queryParamString}"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getRequestUri().toString() >> url

        mvmRequest.putSingle("mail", "test@test.com,test2@test.com")
        mvmRequest.putSingle("date", "2017-01-01")
        mvmRequest.putSingle("range", range)

        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        Integer quantityPerPage = (drc.paginationContext.limit - drc.paginationContext.offset) + 1
        
        String linkHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get(HttpHeaders.LINK)
        result.paginationContext = drc.paginationContext
        String linkHeaderExpected = ""

        // First page
        if(drc.paginationContext.offset != 0){
            def prevRange1 = (offset - quantityPerPage) < 0 ? 0 : (offset - quantityPerPage)
            def firstRange2 = quantityPerPage - 1 < offset - 1 ? quantityPerPage - 1 : offset - 1
            linkHeaderExpected = "<${url}&range=0-${firstRange2}>; rel=\"first\",<${url}&range=${prevRange1}-${offset-1}>; rel=\"prev\","
        }
        
        linkHeaderExpected += "<${url}&range=${result.paginationContext.limit+1}-${result.paginationContext.limit+quantityPerPage}>; rel=\"next\",<${url}&range=${result.count - quantityPerPage}-${result.count -1}>; rel=\"last\""

        then:
        
        linkHeader == linkHeaderExpected 
        
        where:
        defaultPagination   | range     | limit | offset
//        10                  | "10-19"   | 10    | 10
          10                  | "20-29"   | 29    | 20
//        10                  | "2-11"    | 10    | 2
//        10                  | "1-5"     | 10    | 1
//        10                  | "3-7"     | 10    | 3
//        10                  | "50-52"   | 10    | 50
//        10                  | "73-80"   | 10    | 73
//        10                  | "1-1"     | 10    | 1
//        10                  | "2-2"     | 10    | 2
//        25                  | "1-1"     | 25    | 1
//        25                  | "18-30"   | 25    | 18
    }
    
    def "A response header should have a 'Accept-Range' field"(){
        given:
        crudConfig.getDefaultPagination() >> 50
        crudConfig.isPaginationEnabled() >> true
        drc.paginationContext = new PaginationContext(50, 0, true);
        result.paginationContext = drc.paginationContext
        uriInfo.getQueryParameters() >> mvmRequest

        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")

        result.entityClass = UserModelForTest.class

        String expectedRangeHeader = "usermodelfortest 50"
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)

        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Accept-Range").isEmpty()
        
        when: "Set a entityClass"
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Accept-Range").isEmpty()
        String acceptRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Accept-Range")
        acceptRangeHeader == expectedRangeHeader
        
        when: "An entityClass not filled"
        result.entityClass = null
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Accept-Range").isEmpty()
        String acceptRangeHeader2 = paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Accept-Range")
        acceptRangeHeader2 == expectedRangeHeader
    }
    
    def "A response header should have a 'Content-Range' field"(offset, limit, count){
        given:
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getQueryParameters() >> mvmRequest
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")

        mvmRequest.putSingle("range", new String("${offset}-${offset+(offset+count > limit ? limit : offset+count)}"))

        uriInfo.getQueryParameters() >> mvmRequest

        crudConfig.isPaginationEnabled() >> true
        crudConfig.getDefaultPagination() >> 50
        result.paginationContext = new PaginationContext(limit, offset, true)
        drc.paginationContext = result.paginationContext
        result.entityClass = UserModelForTest.class
        result.count = count
        String expectedContentRangeHeader = "${offset}-${limit}/${count}"
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        String contentRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Content-Range")

        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).get("Content-Range").isEmpty()
        contentRangeHeader == expectedContentRangeHeader
        
        where:
        offset  |limit  |count
        0       |10     |20
        11      |15     |50        
    }
    
    def "A request with pagination disabled should not put HTTP headers"(){
        given:
        crudConfig.isPaginationEnabled() >> false
        drc.paginationContext = PaginationContext.disabledPagination()
        drc.entityClass = UserRestForTest.class


        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getQueryParameters() >> mvmRequest
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")

        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).containsKey('Content-Range')
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).containsKey('Accept-Range')
        !paginationHelper.buildHeaders(resourceInfo, uriInfo, result).containsKey('Link')
        
    }
    
    def "A method annotated with @Search annotation should override default configurations"() {
        
        given:
        crudConfig.getDefaultPagination() >> 50
        crudConfig.isPaginationEnabled() >> true
        uriInfo.getQueryParameters() >> mvmRequest

        DemoiselleResult crudAnnotation = UserRestForTest.class.getDeclaredMethod("findWithSearch").getAnnotation(DemoiselleResult.class);
        drc.demoiselleResultAnnotation = crudAnnotation
        Integer quantityPerPage = crudAnnotation.pageSize()
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.paginationContext.limit != 50
        drc.paginationContext.limit == quantityPerPage - 1
        
    }
    
    def "A method annotated with @Search and own withPagination property set with 'true' and default pagination disabled should not be paginated"(){
        
        given:
        crudConfig.isPaginationEnabled() >> false
        uriInfo.getQueryParameters() >> mvmRequest
        
        Boolean withPagination = UserRestForTest.class.getDeclaredMethod("findWithSearch").getAnnotation(DemoiselleResult.class).enablePagination()
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        withPagination == Boolean.TRUE
        crudConfig.paginationEnabled== Boolean.FALSE
        drc.paginationContext.isPaginationEnabled() == Boolean.FALSE
        
    }
    
    def "A method annotated with @Search and own withPagination property set with 'false' and default pagination enabled should be paginated"(){
        
        given:
        crudConfig.isPaginationEnabled() >> true
        crudConfig.getDefaultPagination() >> 50
        result.entityClass = UserModelForTest.class
        uriInfo.getQueryParameters() >> mvmRequest
        drc.demoiselleResultAnnotation = UserRestForTest.class.getDeclaredMethod("findWithSearchAnnotationAndPaginationDisabled").getAnnotation(DemoiselleResult.class)
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAnnotationAndPaginationDisabled")
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.demoiselleResultAnnotation.enablePagination() == Boolean.FALSE
        crudConfig.isPaginationEnabled() == Boolean.TRUE
        drc.paginationContext.isPaginationEnabled() == Boolean.FALSE
        
    }

    private configureRequestForCrud(){
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
    }

}