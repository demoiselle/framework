package org.demoiselle.jee.crud.pagination


import javax.ws.rs.BadRequestException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

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
    MultivaluedMap mvmRequest = Mock()
    MultivaluedMap mvmResponse = Mock()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    ResultSet resultSet = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    PaginationFilter pf = new PaginationFilter(uriInfo, info, resultSet, dpc, message)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        mvmRequest.containsKey("range") >> true
        mvmRequest.get("range") >> ["10-20"] 
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
        mvmRequest.containsKey("range") >> true
        mvmRequest.get("range") >> [parameter]
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
            limit == 10
            content == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        }
        
        responseContext.status == Status.PARTIAL_CONTENT.getStatusCode()
    }
    

}