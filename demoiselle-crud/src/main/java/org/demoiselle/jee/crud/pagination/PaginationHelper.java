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

import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.Search;

/**
 * @author SERPRO
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

    private final String HTTP_HEADER_CONTENT_RANGE = "Content-Range";
    private final String HTTP_HEADER_ACCEPT_RANGE = "Accept-Range";
    private final String HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    

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
    }

    private void fillObjects(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;
    }

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

    private Boolean isRequestPagination() {
        // Verify if contains 'range' in url
        if (uriInfo.getQueryParameters().containsKey(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

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

    public Boolean isPartialContentResponse() {
        Integer limit = drc.getLimit() == null ? 0 : drc.getLimit();
        Long count = drc.getCount() == null ? 0 : drc.getCount();
        return !((limit + 1) >= count);
    }

    private void logInvalidRangeParameters(String range) {
        logger.warning(message.invalidRangeParameters() + ", [params: " + range + "]");
    }

    private String buildContentRange() {
        Integer limit = drc.getLimit() == null ? 0 : drc.getLimit();
        Integer offset = drc.getOffset() == null ? 0 : drc.getOffset();
        Long count = drc.getCount() == null ? 0 : drc.getCount();
        return offset + "-" + (limit.equals(0) ? count - 1 : drc.getLimit()) + "/" + count;
    }

    public String buildAcceptRange() {
        String resource = "";

        if (drc.getEntityClass() != null) {
            resource = drc.getEntityClass().getSimpleName().toLowerCase();
        } else {
            if (resourceInfo.getResourceClass() != null) {
                Class<?> targetClass = CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass());
                if (targetClass != null) {
                    resource = targetClass.getSimpleName().toLowerCase();
                }
            }
        }

        return resource + " " + getDefaultNumberPagination();
    }

    public Map<String, String> buildHeaders(ResourceInfo resourceInfo, UriInfo uriInfo) {
        fillObjects(resourceInfo, uriInfo);
        Map<String, String> headers = new ConcurrentHashMap<>();

        if (drc.isPaginationEnabled()) {
            headers.put(HTTP_HEADER_CONTENT_RANGE, buildContentRange());
            headers.put(HTTP_HEADER_ACCEPT_RANGE, buildAcceptRange());
            String linkHeader = buildLinkHeader();

            if (!linkHeader.isEmpty()) {
                headers.put(HttpHeaders.LINK, linkHeader);
            }
        }
        
        headers.put(HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS, HTTP_HEADER_ACCEPT_RANGE + ", " + HTTP_HEADER_CONTENT_RANGE + ", " + HttpHeaders.LINK);
        
        return headers;
    }

    private String buildLinkHeader() {
        StringBuffer sb = new StringBuffer();
        String url = uriInfo.getRequestUri().toString();
        url = url.replaceFirst(".range=([^&]*)", "");

        if (drc.getOffset() == null && drc.getLimit() == null) {
            drc.setOffset(new Integer(0));
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
            response.getHeaders().putSingle(HTTP_HEADER_ACCEPT_RANGE, buildAcceptRange());
        }

    }

}
