/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedHTTPHeaders;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.Search;

/**
 * Class responsible for managing the 'range' parameter comes from Url Query
 * String.
 *
 * Ex:
 *
 * Given a request
 * <pre>
 * GET @{literal http://localhost:8080/api/users?range=0-10}
 * </pre>
 *
 * This class will processing the request above and parse the range parameters
 * to {@link DemoiselleRequestContext} object.
 *
 * This object will be use on {@link AbstractDAO} class to execute the
 * pagination on database.
 *
 */
@RequestScoped
public class PaginationHelper {

    private UriInfo uriInfo;

    private ResourceInfo resourceInfo;

    @Inject
    private DemoiselleRequestContext drc;

    @Inject
    private PaginationHelperMessage message;

    @Inject
    private PaginationHelperConfig paginationConfig;

    private static final Logger logger = Logger.getLogger(PaginationHelper.class.getName());

    public PaginationHelper() {
    }

    public PaginationHelper(ResourceInfo resourceInfo, UriInfo uriInfo, PaginationHelperConfig paginationConfig, DemoiselleRequestContext drc, PaginationHelperMessage message) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.paginationConfig = paginationConfig;
        this.drc = drc;
        this.message = message;
    }

    /**
     * Open the request query string to extract values from 'range' parameter
     * and fill the {@link DemoiselleRequestContext#setOffset(Integer)} and
     * {@link DemoiselleRequestContext#setLimit(Integer)}
     *
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        fillObjects(resourceInfo, uriInfo);

        drc.setPaginationEnabled(isPaginationEnabled());

        if (drc.isPaginationEnabled()) {

            if (isRequestPagination()) {
                checkAndFillRangeValues();
            }

            if (hasSearchAnnotation() && !isRequestPagination()) {
                drc.setLimit(getDefaultNumberPagination() - 1);
                drc.setOffset(new Integer(0));
            }
        }

        if (hasSearchAnnotation() && isRequestPagination()) {
            Search searchAnnotation = resourceInfo.getResourceMethod().getAnnotation(Search.class);
            // Pagination @Search.withPagination is disabled but the request parameter has 'range' parameter
            if (searchAnnotation.withPagination() == Boolean.FALSE) {
                throw new IllegalArgumentException(message.paginationIsNotEnabled());
            }
        }
    }

    private void fillObjects(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;
    }

    /**
     * Check the pagination is enabled
     *
     * @return pagination enabled/disabled
     */
    private Boolean isPaginationEnabled() {
        if (paginationConfig.getIsGlobalEnabled() == Boolean.FALSE) {
            return Boolean.FALSE;
        }

        if (hasSearchAnnotation()) {
            Search searchAnnotation = resourceInfo.getResourceMethod().getAnnotation(Search.class);
            return searchAnnotation.withPagination();
        }

        return paginationConfig.getIsGlobalEnabled();
    }

    /**
     * Check if the actual request has the 'range' parameter on query string
     *
     * @return is request pagination or not
     */
    private Boolean isRequestPagination() {
        // Verify if contains 'range' in url
        if (uriInfo.getQueryParameters().containsKey(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Check if the value of 'range' parameter is valid using the rules:
     *
     * - Value formatted like offset-limit (range=0-10); - The 'offset' and
     * 'limit' should be a integer; - The 'offset' should be less than or equals
     * 'limit';
     *
     *
     * @throws IllegalArgumentException The format is invalid
     */
    private void checkAndFillRangeValues() throws IllegalArgumentException {
        List<String> rangeList = uriInfo.getQueryParameters().get(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey());
        if (!rangeList.isEmpty()) {
            String range[] = rangeList.get(0).split("-");
            if (range.length == 2) {
                String offset = range[0];
                String limit = range[1];

                try {
                    drc.setOffset(new Integer(offset));
                    drc.setLimit(new Integer(limit));

                    if (drc.getOffset() > drc.getLimit()) {
                        logInvalidRangeParameters(rangeList.get(0));
                        throw new IllegalArgumentException(this.message.invalidRangeParameters());
                    }

                    if (((drc.getLimit() - drc.getOffset()) + 1) > getDefaultNumberPagination()) {
                        logger.warning(message.defaultPaginationNumberExceed(getDefaultNumberPagination()) + ", [" + drc.toString() + "]");
                        throw new IllegalArgumentException(message.defaultPaginationNumberExceed(getDefaultNumberPagination()));
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

    /**
     * Get default pagination number, if the target method is annotated with
     * Search annotation the default annotation will be
     * {@link Search#quantityPerPage()} otherwise the default pagination will be
     * {@link PaginationHelperConfig#getDefaultPagination()} value;
     *
     * @return Number per page
     */
    private Integer getDefaultNumberPagination() {
        if (hasSearchAnnotation()) {
            Search searchAnnotation = resourceInfo.getResourceMethod().getAnnotation(Search.class);
            return searchAnnotation.quantityPerPage();
        }

        return paginationConfig.getDefaultPagination();
    }

    private Boolean hasSearchAnnotation() {
        return resourceInfo.getResourceMethod().isAnnotationPresent(Search.class);
    }

    /**
     * Check if the actual response is a Partial Content (HTTP 206 code)
     *
     * @return is partial content or not
     */
    public Boolean isPartialContentResponse() {
        Integer limit = drc.getLimit() == null ? 0 : drc.getLimit();
        Long count = drc.getCount() == null ? 0 : drc.getCount();
        return !((limit + 1) >= count);
    }

    private void logInvalidRangeParameters(String range) {
        logger.warning(message.invalidRangeParameters() + ", [params: " + range + "]");
    }

    /**
     * Build the 'Content-Range' HTTP Header value.
     *
     * @return 'Content-Range' value
     */
    private String buildContentRange() {
        Integer limit = drc.getLimit() == null ? getDefaultNumberPagination() - 1 : drc.getLimit();
        Integer offset = drc.getOffset() == null ? 0 : drc.getOffset();
        Long count = drc.getCount() == null ? 0 : drc.getCount();
        return offset + "-" + (limit.equals(0) ? count - 1 : limit) + "/" + count;
    }

    /**
     * Build the 'Accept-Range' HTTP Header value.
     *
     * @return 'Accept-Range' value
     */
    public String buildAcceptRange() {
        String resource = "";

        if (drc.getEntityClass() != null) {
            resource = drc.getEntityClass().getSimpleName().toLowerCase();
        } else {
            if (resourceInfo != null && resourceInfo.getResourceClass() != null) {
                Class<?> targetClass = CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass());
                if (targetClass != null) {
                    resource = targetClass.getSimpleName().toLowerCase();
                }
            }
        }

        if (!resource.isEmpty()) {
            return resource + " " + getDefaultNumberPagination();
        }

        return null;
    }

    /**
     * Set the 'Content-Range', 'Accept-Range', 'Link' and
     * 'Access-Control-Expose-Headers' HTTP headers;
     *
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     *
     * @return A map with HTTP headers
     */
    public Map<String, String> buildHeaders(ResourceInfo resourceInfo, UriInfo uriInfo) {
        fillObjects(resourceInfo, uriInfo);
        Map<String, String> headers = new ConcurrentHashMap<>();

        if (drc.isPaginationEnabled()) {
            headers.putIfAbsent(ReservedHTTPHeaders.HTTP_HEADER_CONTENT_RANGE.getKey(), buildContentRange());
            headers.putIfAbsent(ReservedHTTPHeaders.HTTP_HEADER_ACCEPT_RANGE.getKey(), buildAcceptRange());
            String linkHeader = buildLinkHeader();

            if (!linkHeader.isEmpty()) {
                headers.putIfAbsent(HttpHeaders.LINK, linkHeader);
            }
        }

        return headers;
    }

    /**
     * Build the 'Link' HTTP header value
     *
     * @return 'Link' value
     */
    private String buildLinkHeader() {
        StringBuffer sb = new StringBuffer();
        String url = uriInfo.getRequestUri().toString();
        url = url.replaceFirst(".range=([^&]*)", "");

        if (drc.getOffset() == null) {
            drc.setOffset(new Integer(0));
        }

        if (drc.getLimit() == null) {
            drc.setLimit(getDefaultNumberPagination() - 1);
        }

        Integer offset = drc.getOffset() + 1;
        Integer limit = drc.getLimit() + 1;
        Integer quantityPerPage = (limit - offset) + 1;

        if (uriInfo.getQueryParameters().isEmpty()
                || (uriInfo.getQueryParameters().size() == 1 && uriInfo.getQueryParameters().containsKey(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey()))) {
            url += "?" + ReservedKeyWords.DEFAULT_RANGE_KEY.getKey() + "=";
        } else {
            url += "&" + ReservedKeyWords.DEFAULT_RANGE_KEY.getKey() + "=";
        }

        if (!isFirstPage()) {
            Integer prevPageRangeInit = (drc.getOffset() - quantityPerPage) < 0 ? 0 : (drc.getOffset() - quantityPerPage);
            Integer firstRange2 = quantityPerPage - 1 < drc.getOffset() - 1 ? quantityPerPage - 1 : drc.getOffset() - 1;

            String firstPage = url + 0 + "-" + firstRange2;
            String prevPage = url + prevPageRangeInit + "-" + (drc.getOffset() - 1);

            sb.append("<").append(firstPage).append(">; rel=\"first\",");
            sb.append("<").append(prevPage).append(">; rel=\"prev\",");
        }

        if (isPartialContentResponse()) {
            String nextPage = url + (drc.getOffset() + quantityPerPage) + "-" + (2 * quantityPerPage + drc.getOffset() - 1);
            String lastPage = url + (drc.getCount() - quantityPerPage) + "-" + (drc.getCount() - 1);

            if (offset + quantityPerPage >= drc.getCount() - 1) {
                nextPage = lastPage;
            }

            sb.append("<").append(nextPage).append(">; rel=\"next\",");
            sb.append("<").append(lastPage).append(">; rel=\"last\"");
        }

        return sb.toString();
    }

    private Boolean isFirstPage() {
        return drc.getOffset().equals(0);
    }

    public void buildAcceptRangeWithResponse(ContainerResponseContext response) {
        if (response != null) {
            String acceptRangeHeader = buildAcceptRange();
            if (acceptRangeHeader != null) {
                response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_ACCEPT_RANGE.getKey(), acceptRangeHeader);
            }
        }

    }

}
