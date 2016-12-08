package org.demoiselle.jee.persistence.crud.pagination;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.pagination.ResultSet;

@Provider
public class PaginationFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Context
    private ResourceInfo info;
    
    @Context
    private UriInfo uriInfo;

    @Inject
    private ResultSet resultSet;
    
    @Inject
    private DemoisellePaginationConfig paginationConfig;
    
    private final String DEFAULT_RANGE_KEY = "range";
    private final String HTTP_HEADER_CONTENT_RANGE = "Content-Range";
    private final String HTTP_HEADER_ACCEPT_RANGE = "Accept-Range";
    
    @Inject
    private DemoisellePaginationMessage message;
    
    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext response) throws IOException {
    	
        if (response.getEntity() instanceof ResultSet) {
        	
            response.getHeaders().putSingle(HTTP_HEADER_CONTENT_RANGE, buildContentRange());
            response.getHeaders().putSingle(HTTP_HEADER_ACCEPT_RANGE, buildAcceptRange());
            response.setEntity(resultSet.getContent());
            
            if (resultSet.getLimit() >= resultSet.getCount()) {
                response.setStatus(Status.OK.getStatusCode());
            } 
            else {
                response.setStatus(Status.PARTIAL_CONTENT.getStatusCode());
            }
        }
    
    }

	private String buildContentRange() {
		return resultSet.getOffset() + "-" + resultSet.getLimit() + "/" + resultSet.getCount();
	}
	
	private String buildAcceptRange(){
		String resource = "";
		
		if(!resultSet.getContent().isEmpty()){
			resource = resultSet.getContent().get(0).getClass().getSimpleName().toLowerCase();
		}
		
		return resource + " " + paginationConfig.getDefaultPagination();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if(isRequestPagination()){
			System.out.println("PAGINATION >>>>>>>>>>>>>>>>>>>>");
			
			if(hasRangeKey()){
				try{
					checkAndFillRangeValues();
				}
				catch(IllegalArgumentException e){
					throw new BadRequestException(e.getMessage());
				}
			}
			
		}
		
	}

	private void checkAndFillRangeValues() throws IllegalArgumentException{
		List<String> rangeList = uriInfo.getQueryParameters().get(DEFAULT_RANGE_KEY);
		if(!rangeList.isEmpty()){
			String range[] = rangeList.get(0).split("-");
			if(range.length == 2){
				String offset = range[0];
				String limit = range[1];
				
				try{
					resultSet.setOffset(new Integer(offset));
					resultSet.setLimit(new Integer(limit));
					

					if(resultSet.getOffset() > resultSet.getLimit()){
						logInvalidRangeParameters(rangeList.get(0));
						throw new IllegalArgumentException(this.message.invalidRangeParameters());
					}
					
					if(((resultSet.getLimit() - resultSet.getOffset()) + 1) > paginationConfig.getDefaultPagination()){
						logger.warning(message.defaultPaginationNumberExceed(paginationConfig.getDefaultPagination()) + ", [" + resultSet.toString() + "]");
						throw new IllegalArgumentException(message.defaultPaginationNumberExceed(paginationConfig.getDefaultPagination()));
					}
				}
				catch(NumberFormatException nfe){
					logInvalidRangeParameters(rangeList.get(0));
					throw new IllegalArgumentException(message.invalidRangeParameters());
				}
			}
			else{
				logInvalidRangeParameters(rangeList.get(0));
				throw new IllegalArgumentException(message.invalidRangeParameters());
			}
		}
		
	}
	
	private void logInvalidRangeParameters(String range){
		logger.warning(message.invalidRangeParameters() + ", [params: " + range + "]");
	}
	
	private Boolean isRequestPagination() {

		if(hasRangeKey()) {			
			return true;
		}
		
		return false;
	}
	
	private Boolean hasRangeKey(){
		if(this.uriInfo.getQueryParameters().containsKey(DEFAULT_RANGE_KEY)) {			
			return true;
		}
		return false;
	}
    
    
}
