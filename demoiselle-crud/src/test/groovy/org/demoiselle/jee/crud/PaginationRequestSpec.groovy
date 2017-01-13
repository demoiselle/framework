/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud


import javax.ws.rs.BadRequestException
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
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig
import org.demoiselle.jee.crud.pagination.DemoisellePaginationMessage
import org.demoiselle.jee.crud.pagination.ResultSet

import spock.lang.*
import spock.lang.MockingApi.*

/**
 * 
 * @author SERPRO
 *
 */
class PaginationRequestSpec extends Specification {
    
    ContainerRequestContext requestContext = Mock()
    ContainerResponseContext responseContext = Mock()
    ResourceInfo info = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    MultivaluedMap mvmResponse = new MultivaluedHashMap()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    CrudFilter crudFilter = new CrudFilter(uriInfo, info, drc, dpc, message)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        mvmRequest.putSingle("range", "10-20") 
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        crudFilter.filter(requestContext)
        
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
        mvmRequest.putSingle("range", parameter) 
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        crudFilter.filter(requestContext)
        
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
    
    def "A request without 'range' parameter should not populate 'ResultSet' object"() {
        
        given:
        dpc.getDefaultPagination() >> 10
        mvmRequest.containsKey("range") >> false        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        with(drc){
            offset == 0
            limit == 0
            count == 0            
            entityClass == null
        }
    }
    
    def "A valided request should return a response paginated"() {
     
        given:
        dpc.getDefaultPagination() >> 10
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        def contentMock = 1..20
        result.content = contentMock.subList(0, 10)
        drc.count = contentMock.size()
        drc.limit = null
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        uriInfo.getRequestUri() >> uri
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> result
        
        responseContext.getStatus() >> Status.PARTIAL_CONTENT.getStatusCode()
        
        when:
        crudFilter.filter(requestContext, responseContext)
        
        then:
        with(drc) {
            offset == 0
            count == 20
            limit == 9            
        }
        result.content == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        
        responseContext.status == Status.PARTIAL_CONTENT.getStatusCode()
    }
    
    @Unroll
    def "A partial response should build a 'Link' header with parameters: [defaultPagination: #defaultPagination, offset: #offset, limit: #limit]"(defaultPagination, offset, limit){
        
        given:
        dpc.getDefaultPagination() >> defaultPagination
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
        
        requestContext.getHeaders() >> mvmRequest
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> result
        
        responseContext.getStatus() >> Status.PARTIAL_CONTENT.getStatusCode()
        
        when:
        crudFilter.filter(requestContext, responseContext)
        
        Integer quantityPerPage = (drc.limit - drc.offset) + 1
        
        String linkHeader = responseContext.getHeaders().get(HttpHeaders.LINK).first()
        String linkHeaderExpected = ""
        
        // First page
        if(drc.offset != 0){
            def prevRange1 = (offset - quantityPerPage) < 0 ? 0 : (offset - quantityPerPage)
            def firstRange2 = quantityPerPage - 1 < offset - 1 ? quantityPerPage - 1 : offset - 1
            linkHeaderExpected = "<${url}&range=0-${firstRange2}>; rel=\"first\",<${url}&range=${prevRange1}-${offset-1}>; rel=\"prev\","
        }
        
        linkHeaderExpected += "<${url}&range=${drc.limit+1}-${drc.limit+quantityPerPage}>; rel=\"next\",<${url}&range=${drc.count - quantityPerPage}-${drc.count-1}>; rel=\"last\""

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
        uriInfo.getQueryParameters() >> mvmRequest
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        info.getResourceClass() >> UserRestForTest.class 
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> result
        
        String expectedRangeHeader = "usermodelfortest 50"
        
        when:
        crudFilter.filter(requestContext, responseContext)

        then:
        !responseContext.getHeaders().isEmpty()
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        
        when: "Set a entityClass"
        drc.entityClass = UserModelForTest.class
        crudFilter.filter(requestContext, responseContext)
        
        then:
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        String acceptRangeHeader = responseContext.getHeaders().get('Accept-Range').first()
        acceptRangeHeader == expectedRangeHeader
        
        when: "An entityClass not filled"
        drc.entityClass = null
        crudFilter.filter(requestContext, responseContext)
        
        then:
        !responseContext.getHeaders().get('Accept-Range').isEmpty()
        String acceptRangeHeader2 = responseContext.getHeaders().get('Accept-Range').first()
        acceptRangeHeader2 == expectedRangeHeader
    }
    
    def "A response header should have a 'Content-Range' field"(offset, limit, count){
        given:
        
        String url = "http://localhost:9090/api/users"
        
        URI uri = new URI(url)
        
        uriInfo.getRequestUri() >> uri
        uriInfo.getQueryParameters() >> mvmRequest
        
        drc.offset = offset
        drc.limit = limit
        drc.count = count
        
        responseContext.getHeaders() >> mvmResponse
        responseContext.getEntity() >> result
        
        String expectedContentRangeHeader = "${offset}-${limit}/${count}"
        
        when:
        crudFilter.filter(requestContext, responseContext)
        
        String contentRangeHeader = responseContext.getHeaders().get('Content-Range').first()

        then:
        !responseContext.getHeaders().isEmpty()
        !responseContext.getHeaders().get('Content-Range').isEmpty()
        contentRangeHeader == expectedContentRangeHeader
        
        where:
        offset  |limit  |count
        0       |10     |20
        11      |15     |50        
    }
    

}