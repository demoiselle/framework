/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud.pagination;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.persistence.crud.AbstractREST;

/**
 * TODO javadoc
 *
 * @author SERPRO
 *
 */
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

    public PaginationFilter() {
    }

    public PaginationFilter(UriInfo uriInfo, ResourceInfo info, ResultSet resultSet,
            DemoisellePaginationConfig dpc, Logger logger, DemoisellePaginationMessage message) {
        this.uriInfo = uriInfo;
        this.info = info;
        this.resultSet = resultSet;
        this.paginationConfig = dpc;
        this.logger = logger;
        this.message = message;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (isRequestPagination()) {
            try {
                checkAndFillRangeValues();
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext response) throws IOException {

        if (response.getEntity() instanceof ResultSet) {

            buildHeaders(response);

            response.setEntity(resultSet.getContent());

            if (!isPartialContentResponse()) {
                response.setStatus(Status.OK.getStatusCode());
            } else {
                response.setStatus(Status.PARTIAL_CONTENT.getStatusCode());
            }
        } else {
            if (Status.BAD_REQUEST.getStatusCode() == response.getStatus()
                    && resultSet.getEntityClass() == null) {
                response.getHeaders().putSingle(HTTP_HEADER_ACCEPT_RANGE, buildAcceptRange());
            }
        }

    }

    private Boolean isPartialContentResponse() {
        return !((resultSet.getLimit() + 1) >= resultSet.getCount());
    }

    private void buildHeaders(ContainerResponseContext response) {
        response.getHeaders().putSingle(HTTP_HEADER_CONTENT_RANGE, buildContentRange());
        response.getHeaders().putSingle(HTTP_HEADER_ACCEPT_RANGE, buildAcceptRange());

        buildLinkHeader(response);

    }

    private void buildLinkHeader(ContainerResponseContext response) {
        StringBuffer sb = new StringBuffer();
        String url = uriInfo.getRequestUri().toString();
        url = url.replaceFirst("\\?.*$", "");
        url += "?" + DEFAULT_RANGE_KEY + "=";

        if (resultSet.getOffset().equals(0) && resultSet.getLimit().equals(0)) {
            resultSet.setLimit(paginationConfig.getDefaultPagination());
        }

        Integer offset = resultSet.getOffset() + 1;
        Integer limit = resultSet.getLimit() + 1;
        Integer quantityPerPage = (limit - offset) + 1;

        if (!isFirstPage()) {
            String firstPage = url + 0 + "-" + (quantityPerPage - 1);
            String prevPage = url + (offset - quantityPerPage) + "-" + (offset - 1);

            sb.append("<").append(firstPage).append(">; rel=\"first\",");
            sb.append("<").append(prevPage).append(">; rel=\"prev\",");
        }

        if (isPartialContentResponse()) {
            String nextPage = url + (resultSet.getOffset() + quantityPerPage) + "-" + (2 * quantityPerPage + resultSet.getOffset() - 1);
            String lastPage = url + (resultSet.getCount() - quantityPerPage) + "-" + (resultSet.getCount() - 1);

            sb.append("<").append(nextPage).append(">; rel=\"next\",");
            sb.append("<").append(lastPage).append(">; rel=\"last\"");
        }

        response.getHeaders().putSingle(HttpHeaders.LINK, sb.toString());
    }

    private Boolean isFirstPage() {
        return resultSet.getOffset().equals(0);
    }

    private String buildContentRange() {
        return resultSet.getOffset() + "-" + resultSet.getLimit() + "/" + resultSet.getCount();
    }

    private String buildAcceptRange() {
        String resource = "";

        if (resultSet.getEntityClass() != null) {
            resource = resultSet.getEntityClass().getSimpleName().toLowerCase();
        } else {
            if (info.getResourceClass() != null) {
                Class<?> targetClass = info.getResourceClass();
                if (targetClass.getSuperclass().equals(AbstractREST.class)) {
                    Class<?> type = (Class<?>) ((ParameterizedType) targetClass.getGenericSuperclass()).getActualTypeArguments()[0];
                    resource = type.getSimpleName().toLowerCase();
                }
            }
        }

        return resource + " " + paginationConfig.getDefaultPagination();
    }

    private void checkAndFillRangeValues() throws IllegalArgumentException {
        List<String> rangeList = uriInfo.getQueryParameters().get(DEFAULT_RANGE_KEY);
        if (!rangeList.isEmpty()) {
            String range[] = rangeList.get(0).split("-");
            if (range.length == 2) {
                String offset = range[0];
                String limit = range[1];

                try {
                    resultSet.setOffset(new Integer(offset));
                    resultSet.setLimit(new Integer(limit));

                    if (resultSet.getOffset() > resultSet.getLimit()) {
                        logInvalidRangeParameters(rangeList.get(0));
                        throw new IllegalArgumentException(this.message.invalidRangeParameters());
                    }

                    if (((resultSet.getLimit() - resultSet.getOffset()) + 1) > paginationConfig.getDefaultPagination()) {
                        logger.warning(message.defaultPaginationNumberExceed(paginationConfig.getDefaultPagination()) + ", [" + resultSet.toString() + "]");
                        throw new IllegalArgumentException(message.defaultPaginationNumberExceed(paginationConfig.getDefaultPagination()));
                    }

                } catch (NumberFormatException nfe) {
                    logInvalidRangeParameters(rangeList.get(0));
                    throw new IllegalArgumentException(message.invalidRangeParameters());
                }
            } else {
                logInvalidRangeParameters(rangeList.get(0));
                throw new IllegalArgumentException(message.invalidRangeParameters());
            }
        }

    }

    private void logInvalidRangeParameters(String range) {
        logger.warning(message.invalidRangeParameters() + ", [params: " + range + "]");
    }

    private Boolean isRequestPagination() {
        // Verify if contains 'range' in url
        if (uriInfo.getQueryParameters().containsKey(DEFAULT_RANGE_KEY)) {
            return true;
        }
        return false;
    }

}
