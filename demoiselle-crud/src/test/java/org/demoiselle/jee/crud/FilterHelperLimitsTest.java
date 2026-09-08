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

import org.demoiselle.jee.crud.filter.FilterHelper;
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
 * Tests for the CRUD request limits enforced by {@link FilterHelper}.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FilterHelperLimitsTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock CrudMessage crudMessage;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;

    @BeforeEach
    void setUp() throws Exception {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        lenient().when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:9090/api/users"));
        lenient().when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        lenient().when(crudMessage.filterLimitExceeded(anyInt())).thenReturn("too many filters");
        lenient().when(crudMessage.filterValuesLimitExceeded(anyInt())).thenReturn("too many values");
        lenient().when(crudMessage.filterValueLengthExceeded(anyInt())).thenReturn("value too long");
    }

    private FilterHelper newHelper(CrudLimitsConfig limits) {
        return new FilterHelper(resourceInfo, uriInfo, drc, crudMessage, limits);
    }

    @Test
    void withinDefaultLimitsDoesNotThrow() {
        mvmRequest.putSingle("name", "john");
        FilterHelper helper = newHelper(new CrudLimitsConfig());
        // find() has no @Search so field validation applies; name is a valid field
        assertDoesNotThrow(() -> helper.execute(resourceInfo, uriInfo));
    }

    @Test
    void tooManyFiltersIsRejected() {
        // default maxFilters = 20; add 21 distinct filters
        for (int i = 0; i < 21; i++) {
            mvmRequest.putSingle("field" + i, "value" + i);
        }
        FilterHelper helper = newHelper(new CrudLimitsConfig());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> helper.execute(resourceInfo, uriInfo));
        assertEquals("too many filters", ex.getMessage());
        verify(crudMessage).filterLimitExceeded(20);
    }

    @Test
    void tooManyTotalValuesIsRejected() {
        // default maxFilterValues = 100; spread 101 values across 2 filters (under maxFilters)
        StringBuilder sbA = new StringBuilder();
        for (int i = 0; i < 60; i++) {
            sbA.append("a").append(i).append(i == 59 ? "" : ",");
        }
        StringBuilder sbB = new StringBuilder();
        for (int i = 0; i < 41; i++) {
            sbB.append("b").append(i).append(i == 40 ? "" : ",");
        }
        mvmRequest.putSingle("fieldA", sbA.toString());
        mvmRequest.putSingle("fieldB", sbB.toString());

        FilterHelper helper = newHelper(new CrudLimitsConfig());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> helper.execute(resourceInfo, uriInfo));
        assertEquals("too many values", ex.getMessage());
        verify(crudMessage).filterValuesLimitExceeded(100);
    }

    @Test
    void tooLongFilterValueIsRejected() {
        // default maxFilterValueLength = 1024
        mvmRequest.putSingle("name", "x".repeat(1025));
        FilterHelper helper = newHelper(new CrudLimitsConfig());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> helper.execute(resourceInfo, uriInfo));
        assertEquals("value too long", ex.getMessage());
        verify(crudMessage).filterValueLengthExceeded(1024);
    }

    @Test
    void nullLimitsConfigFallsBackToDefaults() {
        for (int i = 0; i < 21; i++) {
            mvmRequest.putSingle("field" + i, "value" + i);
        }
        FilterHelper helper = newHelper(null);
        assertThrows(IllegalArgumentException.class, () -> helper.execute(resourceInfo, uriInfo));
    }
}
