/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage;
import org.demoiselle.jee.crud.pagination.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test of {@link PaginationHelper} class.
 * Migrated from Spock/Groovy to JUnit 5 + Mockito.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaginationHelperTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock PaginationHelperMessage message;
    @Mock PaginationHelperConfig dpc;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;
    PaginationHelper paginationHelper;

    @BeforeEach
    void setUp() {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        paginationHelper = new PaginationHelper(resourceInfo, uriInfo, dpc, drc, message);
    }

    private void configureRequestForCrud() throws Exception {
        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        URI uri = new URI("http://localhost:9090/api/users");
        lenient().when(uriInfo.getRequestUri()).thenReturn(uri);
    }

    @Test
    void requestWithRangeParameterShouldFillResultObject() throws Exception {
        configureRequestForCrud();
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        mvmRequest.putSingle("range", "10-20");
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        paginationHelper.execute(resourceInfo, uriInfo);

        assertEquals(10, drc.getOffset());
        assertEquals(20, drc.getLimit());
    }

    @ParameterizedTest
    @CsvSource({
        "1, 0",
        "-1, 0",
        "a, 10",
        "10, a",
        "3, ''",
        "'', ''",
        "a, -",
        "-3, -2",
        "-3, -4",
        "-3, -3",
        "0, 10"
    })
    void requestWithInvalidRangeParametersShouldThrowRuntimeException(String offset, String limit) throws Exception {
        configureRequestForCrud();
        String parameter = offset + "-" + limit;
        when(dpc.getDefaultPagination()).thenReturn(10);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        mvmRequest.putSingle("range", parameter);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        assertThrows(RuntimeException.class, () -> paginationHelper.execute(resourceInfo, uriInfo));
    }

    @Test
    void requestWithoutRangeParameterShouldNotPopulateResult() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(10);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        configureRequestForCrud();

        paginationHelper.execute(resourceInfo, uriInfo);

        assertNull(drc.getOffset());
        assertNull(drc.getLimit());
        assertNull(drc.getCount());
        assertNull(drc.getEntityClass());
    }

    @Test
    void responseHeaderShouldHaveAcceptRangeField() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(50);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));

        paginationHelper.execute(resourceInfo, uriInfo);
        assertFalse(paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range").isEmpty());

        // Set entityClass
        drc.setEntityClass(UserModelForTest.class);
        paginationHelper.execute(resourceInfo, uriInfo);
        String acceptRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range");
        assertEquals("usermodelfortest 50", acceptRangeHeader);

        // entityClass not filled
        drc.setEntityClass(null);
        paginationHelper.execute(resourceInfo, uriInfo);
        String acceptRangeHeader2 = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Accept-Range");
        assertEquals("usermodelfortest 50", acceptRangeHeader2);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 10, 20",
        "11, 15, 50"
    })
    void responseHeaderShouldHaveContentRangeField(int offset, int limit, long count) throws Exception {
        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        when(dpc.getIsGlobalEnabled()).thenReturn(true);

        drc.setOffset(offset);
        drc.setLimit(limit);
        drc.setCount(count);

        String expectedContentRangeHeader = offset + "-" + limit + "/" + count;

        paginationHelper.execute(resourceInfo, uriInfo);
        String contentRangeHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get("Content-Range");

        assertFalse(contentRangeHeader.isEmpty());
        assertEquals(expectedContentRangeHeader, contentRangeHeader);
    }

    @Test
    void requestWithPaginationDisabledShouldNotPutHttpHeaders() throws Exception {
        when(dpc.getIsGlobalEnabled()).thenReturn(false);
        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));

        paginationHelper.execute(resourceInfo, uriInfo);

        assertFalse(paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey("Content-Range"));
        assertFalse(paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey("Accept-Range"));
        assertFalse(paginationHelper.buildHeaders(resourceInfo, uriInfo).containsKey("Link"));
    }

    @Test
    void methodWithSearchAnnotationShouldOverrideDefaultConfigurations() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(50);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        int quantityPerPage = UserRestForTest.class.getDeclaredMethod("findWithSearch")
                .getAnnotation(Search.class).quantityPerPage();

        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearch"));

        paginationHelper.execute(resourceInfo, uriInfo);

        assertNotEquals(50, drc.getLimit());
        assertEquals(quantityPerPage - 1, drc.getLimit());
    }

    @Test
    void methodWithSearchAndPaginationTrueButGlobalDisabledShouldNotBePaginated() throws Exception {
        when(dpc.getIsGlobalEnabled()).thenReturn(false);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        boolean withPagination = UserRestForTest.class.getDeclaredMethod("findWithSearch")
                .getAnnotation(Search.class).withPagination();

        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearch"));

        paginationHelper.execute(resourceInfo, uriInfo);

        assertTrue(withPagination);
        assertFalse(dpc.getIsGlobalEnabled());
        assertFalse(drc.isPaginationEnabled());
    }

    @Test
    void methodWithSearchPaginationFalseButGlobalEnabledShouldNotBePaginated() throws Exception {
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        boolean withPagination = UserRestForTest.class.getDeclaredMethod("findWithSearchAnnotationAndPaginationDisabled")
                .getAnnotation(Search.class).withPagination();

        String url = "http://localhost:9090/api/users";
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearchAnnotationAndPaginationDisabled"));

        paginationHelper.execute(resourceInfo, uriInfo);

        assertFalse(withPagination);
        assertTrue(dpc.getIsGlobalEnabled());
        assertFalse(drc.isPaginationEnabled());
    }

    static Stream<Arguments> partialResponseLinkHeaderParams() {
        return Stream.of(
            Arguments.of(10, 10, 19),
            Arguments.of(10, 2, 10),
            Arguments.of(10, 0, 5),
            Arguments.of(10, 3, 7),
            Arguments.of(10, 50, 52),
            Arguments.of(10, 73, 80),
            Arguments.of(10, 1, 1),
            Arguments.of(10, 0, 0),
            Arguments.of(25, 0, 0),
            Arguments.of(25, 18, 30)
        );
    }

    @ParameterizedTest
    @MethodSource("partialResponseLinkHeaderParams")
    void partialResponseShouldBuildLinkHeader(int defaultPagination, int offset, int limit) throws Exception {
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        when(dpc.getDefaultPagination()).thenReturn(defaultPagination);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);

        drc.setOffset(offset);
        drc.setLimit(limit);
        drc.setCount(100L);

        String queryParamString = "?date=2017-01-01&mail=test@test.com,test2@test.com";
        String url = "http://localhost:9090/api/users" + queryParamString;
        URI uri = new URI(url);
        when(uriInfo.getRequestUri()).thenReturn(uri);

        mvmRequest.putSingle("mail", "test@test.com,test2@test.com");
        mvmRequest.putSingle("date", "2017-01-01");
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        paginationHelper.execute(resourceInfo, uriInfo);

        int quantityPerPage = (drc.getLimit() - drc.getOffset()) + 1;
        String linkHeader = paginationHelper.buildHeaders(resourceInfo, uriInfo).get(HttpHeaders.LINK);

        assertNotNull(linkHeader, "Link header should not be null for partial response");

        // Build expected link header
        StringBuilder linkHeaderExpected = new StringBuilder();
        if (drc.getOffset() != 0) {
            int prevRange1 = (offset - quantityPerPage) < 0 ? 0 : (offset - quantityPerPage);
            int firstRange2 = quantityPerPage - 1 < offset - 1 ? quantityPerPage - 1 : offset - 1;
            linkHeaderExpected.append("<").append(url).append("&range=0-").append(firstRange2).append(">; rel=\"first\",");
            linkHeaderExpected.append("<").append(url).append("&range=").append(prevRange1).append("-").append(offset - 1).append(">; rel=\"prev\",");
        }
        linkHeaderExpected.append("<").append(url).append("&range=").append(drc.getLimit() + 1).append("-").append(drc.getLimit() + quantityPerPage).append(">; rel=\"next\",");
        linkHeaderExpected.append("<").append(url).append("&range=").append(drc.getCount() - quantityPerPage).append("-").append(drc.getCount() - 1).append(">; rel=\"last\"");

        assertEquals(linkHeaderExpected.toString(), linkHeader);
    }
}
