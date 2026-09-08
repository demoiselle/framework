/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the effective page-size cap driven by
 * {@link PaginationHelperConfig#getMaxPagination()}.
 *
 * <p>The effective page size is {@code min(default or @Search.quantityPerPage, max)}.
 * A {@code null} or {@code 0} max is treated as "no ceiling" to preserve the
 * behaviour of legacy mocks that do not stub {@code getMaxPagination()}.</p>
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaginationHelperMaxTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock PaginationHelperMessage message;
    @Mock PaginationHelperConfig dpc;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;
    PaginationHelper paginationHelper;

    @BeforeEach
    void setUp() throws Exception {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        paginationHelper = new PaginationHelper(resourceInfo, uriInfo, dpc, drc, message);

        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:9090/api/users"));
        lenient().when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        lenient().when(dpc.getIsGlobalEnabled()).thenReturn(true);
    }

    /**
     * The 'Accept-Range' header value ends with the effective (possibly capped)
     * default page size, making it a convenient probe for the cap logic.
     */
    private int effectiveDefaultPagination(String methodName) throws Exception {
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod(methodName));
        drc.setEntityClass(org.demoiselle.jee.crud.entity.UserModelForTest.class);
        paginationHelper.execute(resourceInfo, uriInfo);
        String acceptRange = paginationHelper.buildAcceptRange();
        return Integer.parseInt(acceptRange.substring(acceptRange.lastIndexOf(' ') + 1));
    }

    @Test
    void defaultConfigMaxPaginationIsHundred() {
        assertEquals(100, new PaginationHelperConfig().getMaxPagination());
    }

    @Test
    void effectiveLimitIsCappedByMax() throws Exception {
        // default pagination 500 but max 100 -> effective 100
        when(dpc.getDefaultPagination()).thenReturn(500);
        when(dpc.getMaxPagination()).thenReturn(100);

        assertEquals(100, effectiveDefaultPagination("find"));
    }

    @Test
    void effectiveLimitUnchangedWhenBelowMax() throws Exception {
        // default pagination 30, max 100 -> effective 30
        when(dpc.getDefaultPagination()).thenReturn(30);
        when(dpc.getMaxPagination()).thenReturn(100);

        assertEquals(30, effectiveDefaultPagination("find"));
    }

    @Test
    void zeroMaxIsTreatedAsNoCeiling() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(500);
        when(dpc.getMaxPagination()).thenReturn(0);

        assertEquals(500, effectiveDefaultPagination("find"));
    }

    @Test
    void nullMaxIsTreatedAsNoCeiling() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(500);
        when(dpc.getMaxPagination()).thenReturn(null);

        assertEquals(500, effectiveDefaultPagination("find"));
    }

    @Test
    void searchAnnotationQuantityIsCappedByMax() throws Exception {
        // @Search findWithSearch quantityPerPage = 10, max = 5 -> effective 5
        when(dpc.getMaxPagination()).thenReturn(5);

        assertEquals(5, effectiveDefaultPagination("findWithSearch"));
    }

    @Test
    void searchAnnotationQuantityUnchangedWhenBelowMax() throws Exception {
        // @Search findWithSearch quantityPerPage = 10, max = 100 -> effective 10
        when(dpc.getMaxPagination()).thenReturn(100);

        assertEquals(10, effectiveDefaultPagination("findWithSearch"));
    }
}
