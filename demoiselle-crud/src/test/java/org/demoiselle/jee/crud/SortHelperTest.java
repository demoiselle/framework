/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortHelper;
import org.demoiselle.jee.crud.sort.SortHelperMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test of {@link SortHelper} class.
 * Migrated from Spock/Groovy to JUnit 5 + Mockito.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
class SortHelperTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock SortHelperMessage message;
    @Mock CrudMessage crudMessage;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;
    SortHelper sortHelper;

    @BeforeEach
    void setUp() {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        sortHelper = new SortHelper(resourceInfo, uriInfo, drc, message, crudMessage);
    }

    private void configureForMethod(String methodName) throws Exception {
        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod(methodName));
        URI uri = new URI("http://localhost:9090/api/users");
        lenient().when(uriInfo.getRequestUri()).thenReturn(uri);
        lenient().when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
    }

    @Test
    void requestWithSortShouldPopulateSorts() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name"));

        sortHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getSorts());
        assertEquals(2, drc.getSorts().size());
        assertEquals("id", drc.getSorts().get(0).field());
        assertEquals(CrudSort.ASC, drc.getSorts().get(0).type());
        assertEquals("name", drc.getSorts().get(1).field());
        assertEquals(CrudSort.ASC, drc.getSorts().get(1).type());
    }

    @Test
    void requestWithSortAndDescWithoutParametersShouldAddAllAsDesc() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name"));
        mvmRequest.putSingle("desc", "");

        sortHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getSorts());
        assertEquals(2, drc.getSorts().size());
        assertEquals("id", drc.getSorts().get(0).field());
        assertEquals(CrudSort.DESC, drc.getSorts().get(0).type());
        assertEquals("name", drc.getSorts().get(1).field());
        assertEquals(CrudSort.DESC, drc.getSorts().get(1).type());
    }

    @Test
    void requestWithSortAndDescWithParametersShouldSeparateDescAndAsc() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name"));
        mvmRequest.putSingle("desc", "name");

        sortHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getSorts());
        assertEquals(2, drc.getSorts().size());
        assertEquals("id", drc.getSorts().get(0).field());
        assertEquals(CrudSort.ASC, drc.getSorts().get(0).type());
        assertEquals("name", drc.getSorts().get(1).field());
        assertEquals(CrudSort.DESC, drc.getSorts().get(1).type());
    }

    @Test
    void requestWithSortAndDescShouldRespectOrder() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name", "mail"));
        mvmRequest.addAll("desc", List.of("name", "mail"));

        sortHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getSorts());
        assertEquals(CrudSort.ASC, drc.getSorts().get(0).type());
        assertEquals("id", drc.getSorts().get(0).field());
        assertEquals(CrudSort.DESC, drc.getSorts().get(1).type());
        assertEquals("name", drc.getSorts().get(1).field());
        assertEquals(CrudSort.DESC, drc.getSorts().get(2).type());
        assertEquals("mail", drc.getSorts().get(2).field());
    }

    @Test
    void requestWithoutSortButWithDescShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("fields", List.of("id", "name", "mail"));
        mvmRequest.putSingle("desc", "name");

        when(message.descParameterWithoutSortParameter()).thenReturn("desc without sort");

        assertThrows(IllegalArgumentException.class, () -> sortHelper.execute(resourceInfo, uriInfo));
        verify(message).descParameterWithoutSortParameter();
    }

    @Test
    void requestWithSortAndInvalidValueShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name", "invalidField"));

        assertThrows(IllegalArgumentException.class, () -> sortHelper.execute(resourceInfo, uriInfo));
    }

    @Test
    void requestWithSortNotMatchingSearchFieldsShouldThrowRuntimeException() throws Exception {
        configureForMethod("findWithSearchAndFields");
        mvmRequest.addAll("sort", List.of("id", "name", "invalidField"));

        when(crudMessage.fieldRequestDoesNotExistsOnSearchField("invalidField"))
                .thenReturn("Field does not exist");

        assertThrows(RuntimeException.class, () -> sortHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnSearchField("invalidField");
    }

    @Test
    void requestWithDescNotMatchingSortShouldThrowRuntimeException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id", "name"));
        mvmRequest.putSingle("desc", "id1");

        when(crudMessage.fieldRequestDoesNotExistsOnSearchField("id1"))
                .thenReturn("Field does not exist");

        assertThrows(RuntimeException.class, () -> sortHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnSearchField("id1");
    }
}
