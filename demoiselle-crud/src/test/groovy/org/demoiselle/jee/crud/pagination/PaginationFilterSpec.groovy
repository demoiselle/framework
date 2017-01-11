/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination


import javax.ws.rs.BadRequestException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

import org.demoiselle.jee.crud.pagination.entity.UserModelForTest
import org.demoiselle.jee.crud.pagination.UserRestForTest

import spock.lang.*
import spock.lang.MockingApi.*

/**
 * 
 * @author SERPRO
 *
 */
class PaginationFilterSpec extends Specification {
    
    ContainerRequestContext requestContext = Mock()
    ContainerResponseContext responseContext = Mock()
    ResourceInfo info = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    MultivaluedMap mvmResponse = new MultivaluedHashMap()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    ResultSet resultSet = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    PaginationFilter pf = new PaginationFilter(uriInfo, info, resultSet, dpc, message)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        mvmRequest.putSingle("range", "10-20") 
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        
        pf.filter(requestContext)
        
        then:
        resultSet.offset == 10
        resultSet.limit == 20
        notThrown(BadRequestException)
        
    }
    
    @Unroll
    def "A request with invalid 'range' parameters (#offset, #limit) should throw RuntimeException"(offset, limit) {
        given:
        
        String parameter = "${offset}-${limit}".toString()
        
        dpc.getDefaultPagination() >> 10
        mvmRequest.putSingle("range", parameter) 
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        
        pf.filter(requestContext)
        
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
        0       | 10 // Greater then default pagination
        
    }
    
    def "A request without 'range' parameter should not populate 'ResultSet' object"() {
        
        given:
        dpc.getDefaultPagination() >> 10
        mvmRequest.containsKey("range") >> false        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        pf.filter(requestContext)
        
        then:
        with(resultSet){
            offset == 0
            limit == 0
            count == 0
            content == []
            entityClass == null
        }
    }
    
    def "A valided request should return a response paginated"() {
     
        given:
        dpc.getDefaultPagination() >> 10
        
        def contentMock = 1..20
        resultSet.content = contentMock.subList(0, 10)
        resultSet.count = contentMock.size()
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        uriInfo.getRequestUri() >> uri
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> resultSet
        
        responseContext.getStatus() >> Status.PARTIAL_CONTENT.getStatusCode()
        
        when:
        pf.filter(requestContext, responseContext)
        
        then:
        with(resultSet) {
            offset == 0
            count == 20
            limit == 9
            content == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        }
        
        responseContext.status == Status.PARTIAL_CONTENT.getStatusCode()
    }
    
    @Unroll
    def "A partial response should build a 'Link' header with parameters: [defaultPagination: #defaultPagination, offset: #offset, limit: #limit]"(defaultPagination, offset, limit){
        
        given:
        dpc.getDefaultPagination() >> defaultPagination
        
        def contentMock = 1..100
        resultSet.offset = offset
        resultSet.limit = limit
        resultSet.content = contentMock.subList(resultSet.offset, resultSet.limit + 1)
        resultSet.count = contentMock.size()
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> resultSet
        
        responseContext.getStatus() >> Status.PARTIAL_CONTENT.getStatusCode()
        
        when:
        pf.filter(requestContext, responseContext)
        
        Integer quantityPerPage = (resultSet.limit - resultSet.offset) + 1
        
        String linkHeader = responseContext.getHeaders().get(HttpHeaders.LINK).first()
        String linkHeaderExpected = ""
        
        // First page
        if(resultSet.offset != 0){
            def prevRange1 = (offset - quantityPerPage) < 0 ? 0 : (offset - quantityPerPage)
            def firstRange2 = quantityPerPage - 1 < offset - 1 ? quantityPerPage - 1 : offset - 1
            linkHeaderExpected = "<${url}?range=0-${firstRange2}>; rel=\"first\",<${url}?range=${prevRange1}-${offset-1}>; rel=\"prev\","
        }
        
        linkHeaderExpected += "<${url}?range=${resultSet.limit+1}-${resultSet.limit+quantityPerPage}>; rel=\"next\",<${url}?range=${resultSet.count - quantityPerPage}-${resultSet.count-1}>; rel=\"last\""

        then:
        !responseContext.getHeaders().isEmpty()
        
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
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        info.getResourceClass() >> UserRestForTest.class 
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> resultSet
        
        String expectedRangeHeader = "usermodelfortest 50"
        
        when:
        pf.filter(requestContext, responseContext)

        then:
        !responseContext.getHeaders().isEmpty()
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        
        when: "Set a entityClass"
        resultSet.entityClass = UserModelForTest.class
        pf.filter(requestContext, responseContext)
        
        then:
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        String acceptRangeHeader = responseContext.getHeaders().get('Accept-Range').first()
        acceptRangeHeader == expectedRangeHeader
        
        when: "An entityClass not filled"
        resultSet.entityClass = null
        pf.filter(requestContext, responseContext)
        
        then:
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        String acceptRangeHeader2 = responseContext.getHeaders().get('Accept-Range').first()
        acceptRangeHeader2 == expectedRangeHeader
    }

}