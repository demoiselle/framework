/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud


import javax.ws.rs.BadRequestException
import javax.ws.rs.GET
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.filter.FilterHelper
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig
import org.demoiselle.jee.crud.pagination.DemoisellePaginationMessage
import org.demoiselle.jee.crud.pagination.PaginationHelper
import org.demoiselle.jee.crud.pagination.ResultSet
import org.demoiselle.jee.crud.sort.SortHelper

import spock.lang.*

import java.lang.reflect.Method
import java.util.concurrent.ForkJoinTask.RunnableExecuteAction

/**
 * 
 * @author SERPRO
 *
 */
class PaginationHelperSpec extends Specification {
    
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    
    ResourceInfo resourceInfo = Mock()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    PaginationHelper paginationHelper = new PaginationHelper(resourceInfo, uriInfo, dpc, drc)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        dpc.getIsGlobalEnabled() >> true
        mvmRequest.putSingle("range", "10-20") 
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.offset == 10
        drc.limit == 20
        notThrown(BadRequestException)
        
    }
    
    @Unroll
    def "A request with invalid 'range' parameters (#offset, #limit) should throw RuntimeException"(offset, limit) {
        given:
        
        String parameter = "${offset}-${limit}".toString()
        
        dpc.getDefaultPagination() >> 10
        dpc.getIsGlobalEnabled() >> true
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
    
    def "A request without 'range' parameter should not populate 'Result' object"() {
        
        given:
        dpc.getDefaultPagination() >> 10
        dpc.getIsGlobalEnabled() >> true
        mvmRequest.containsKey("range") >> false        
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        with(drc){
            offset == null
            limit == null
            count == null         
            entityClass == null
        }
    }
    
    @Unroll
    def "A partial response should build a 'Link' header with parameters: [defaultPagination: #defaultPagination, offset: #offset, limit: #limit]"(defaultPagination, offset, limit){
        
        given:
        dpc.getDefaultPagination() >> defaultPagination
        dpc.getIsGlobalEnabled() >> true
        uriInfo.getQueryParameters() >> mvmRequest
        
        def contentMock = 1..100
        drc.offset = offset
        drc.limit = limit
        result.content = contentMock.subList(drc.offset, drc.limit + 1)
        drc.count = contentMock.size()
        
        String queryParamString = "?date=2017-01-01&mail=test@test.com,test2@test.com"
        String url = "http://localhost:9090/api/users${queryParamString}"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getRequestUri().toString() >> url
        
        mvmRequest.putSingle("mail", "test@test.com,test2@test.com")
        mvmRequest.putSingle("date", "2017-01-01")
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        Integer quantityPerPage = (drc.limit - drc.offset) + 1
        
        String linkHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get(HttpHeaders.LINK)
        String linkHeaderExpected = ""
        
        // First page
        if(drc.offset != 0){
            def prevRange1 = (offset - quantityPerPage) < 0 ? 0 : (offset - quantityPerPage)
            def firstRange2 = quantityPerPage - 1 < offset - 1 ? quantityPerPage - 1 : offset - 1
            linkHeaderExpected = "<${url}&range=0-${firstRange2}>; rel=\"first\",<${url}&range=${prevRange1}-${offset-1}>; rel=\"prev\","
        }
        
        linkHeaderExpected += "<${url}&range=${drc.limit+1}-${drc.limit+quantityPerPage}>; rel=\"next\",<${url}&range=${drc.count - quantityPerPage}-${drc.count-1}>; rel=\"last\""

        then:
        
        linkHeader == linkHeaderExpected 
        
        where:
        defaultPagination   |offset |limit
        10                  |10     |19 
        10                  |2      |10
        10                  |0      |5
        10                  |3      |7
        10                  |50     |52
        10                  |73     |80
        10                  |1      |1
        10                  |0      |0
        25                  |0      |0
        25                  |18     |30
    }
    
    def "A response header should have a 'Accept-Range' field"(){
        given:
        dpc.getDefaultPagination() >> 50
        dpc.getIsGlobalEnabled() >> true
        uriInfo.getQueryParameters() >> mvmRequest
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        resourceInfo.getResourceClass() >> UserRestForTest.class 
        
        String expectedRangeHeader = "usermodelfortest 50"
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)

        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range").isEmpty()
        
        when: "Set a entityClass"
        drc.entityClass = UserModelForTest.class
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range").isEmpty()
        String acceptRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range")
        acceptRangeHeader == expectedRangeHeader
        
        when: "An entityClass not filled"
        drc.entityClass = null
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range").isEmpty()
        String acceptRangeHeader2 = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range")
        acceptRangeHeader2 == expectedRangeHeader
    }
    
    def "A response header should have a 'Content-Range' field"(offset, limit, count){
        given:
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getQueryParameters() >> mvmRequest
        
        dpc.getIsGlobalEnabled() >> true
        
        drc.offset = offset
        drc.limit = limit
        drc.count = count
        
        String expectedContentRangeHeader = "${offset}-${limit}/${count}"
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        String contentRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Content-Range")

        then:
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Content-Range").isEmpty()
        contentRangeHeader == expectedContentRangeHeader
        
        where:
        offset  |limit  |count
        0       |10     |20
        11      |15     |50        
    }
    
    def "A request with pagination disabled should not put HTTP headers"(){
        given:
        dpc.getIsGlobalEnabled() >> false
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        paginationHelper.execute(resourceInfo, uriInfo)
        
        then:        
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey('Content-Range')
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey('Accept-Range')
        !paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey('Links')
        
    }

    private configureRequestForCrud(){
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> AbstractREST.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
    }

}