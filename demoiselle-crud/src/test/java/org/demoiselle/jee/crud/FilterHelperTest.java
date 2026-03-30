/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.filter.FilterHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
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
 * Test of {@link FilterHelper} class.
 * Migrated from Spock/Groovy to JUnit 5 + Mockito.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FilterHelperTest {

    @Mock ContainerRequestContext requestContext;
    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock CrudMessage crudMessage;
    @Mock PaginationHelperConfig dpc;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;
    FilterHelper filterHelper;

    @BeforeEach
    void setUp() {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        filterHelper = new FilterHelper(resourceInfo, uriInfo, drc, crudMessage);
    }

    @Test
    void requestWithFilterShouldPopulateFilters() throws Exception {
        mvmRequest.addAll("mail", List.of("john@test.com", "john2@test.com", "john3@test.com"));
        mvmRequest.putSingle("name", "john john");
        mvmRequest.putSingle("range", "10-20");

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        filterHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getFilters());
        assertTrue(drc.getFilters().containsKey("mail"));
        TreeNodeField<String, Set<String>> mailNode = drc.getFilters().getChildByKey("mail");
        assertEquals(Set.of("john@test.com", "john2@test.com", "john3@test.com"), mailNode.getValue());

        assertTrue(drc.getFilters().containsKey("name"));
        TreeNodeField<String, Set<String>> nameNode = drc.getFilters().getChildByKey("name");
        assertEquals(Set.of("john john"), nameNode.getValue());

        assertFalse(drc.getFilters().containsKey("range"));
    }

    @Test
    void requestWithFilterAndSearchAnnotationShouldValidateFields() throws Exception {
        mvmRequest.putSingle("name", "john john");
        mvmRequest.put("mail", List.of("john@test.com", "john2@test.com", "john3@test.com"));

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearch"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);

        assertThrows(RuntimeException.class, () -> filterHelper.execute(resourceInfo, uriInfo));
    }

    @Test
    void requestWithFilterSubfieldAndSearchAnnotationShouldValidate() throws Exception {
        mvmRequest.putSingle("name", "john john");
        mvmRequest.putSingle("address(street,invalidField)", "my street");

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearch"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(crudMessage.fieldRequestDoesNotExistsOnObject("invalidField", "org.demoiselle.jee.crud.entity.AddressModelForTest"))
                .thenReturn("Field 'invalidField' does not exist on 'AddressModelForTest'");

        assertThrows(IllegalArgumentException.class, () -> filterHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnObject("invalidField", "org.demoiselle.jee.crud.entity.AddressModelForTest");
    }
}
