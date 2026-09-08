/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.sort.SortHelper;
import org.demoiselle.jee.crud.sort.SortHelperMessage;
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
 * Tests for the CRUD request limits enforced by {@link SortHelper}.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SortHelperLimitsTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock SortHelperMessage sortHelperMessage;
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
        lenient().when(crudMessage.sortFieldsLimitExceeded(anyInt())).thenReturn("too many sort fields");
    }

    private SortHelper newHelper(CrudLimitsConfig limits) {
        return new SortHelper(resourceInfo, uriInfo, drc, sortHelperMessage, crudMessage, limits);
    }

    @Test
    void withinDefaultSortLimitDoesNotThrow() {
        mvmRequest.addAll("sort", List.of("id", "name"));
        SortHelper helper = newHelper(new CrudLimitsConfig());
        assertDoesNotThrow(() -> helper.execute(resourceInfo, uriInfo));
    }

    @Test
    void tooManySortFieldsIsRejected() {
        // default maxSortFields = 10; supply 11 valid fields (id/name/mail repeated)
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            fields.add("id");
        }
        mvmRequest.addAll("sort", fields);

        SortHelper helper = newHelper(new CrudLimitsConfig());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> helper.execute(resourceInfo, uriInfo));
        assertEquals("too many sort fields", ex.getMessage());
        verify(crudMessage).sortFieldsLimitExceeded(10);
    }

    @Test
    void nullLimitsConfigFallsBackToDefaults() {
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            fields.add("id");
        }
        mvmRequest.addAll("sort", fields);

        SortHelper helper = newHelper(null);
        assertThrows(IllegalArgumentException.class, () -> helper.execute(resourceInfo, uriInfo));
    }
}
