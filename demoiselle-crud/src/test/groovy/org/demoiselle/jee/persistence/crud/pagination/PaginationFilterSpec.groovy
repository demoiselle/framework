package org.demoiselle.jee.persistence.crud.pagination


import java.util.ArrayList
import java.util.List
import java.util.logging.Logger

import javax.ws.rs.BadRequestException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.UriInfo

import spock.lang.*
import spock.lang.MockingApi.*

/**
 * 
 * @author SERPRO
 *
 */
class PaginationFilterSpec extends Specification {
    
    ContainerRequestContext requestContext = Mock()
    ResourceInfo info = Mock()
    MultivaluedMap mvm = Mock()
    UriInfo uriInfo = Mock()
    Logger logger = Mock()
    DemoisellePaginationMessage message = Mock()
    
    ResultSet resultSet = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    PaginationFilter pf = new PaginationFilter(uriInfo, info, resultSet, dpc, logger, message)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        mvm.containsKey("range") >> true
        mvm.get("range") >> ["10-20"] 
        uriInfo.getQueryParameters() >> mvm
        
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
        mvm.containsKey("range") >> true
        mvm.get("range") >> [parameter]
        uriInfo.getQueryParameters() >> mvm
        
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
        mvm.containsKey("range") >> false        
        uriInfo.getQueryParameters() >> mvm
        
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
    

}