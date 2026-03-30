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

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.field.FieldHelperMessage;
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
 * Test of {@link FieldHelper} class.
 * Migrated from Spock/Groovy to JUnit 5 + Mockito.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FieldHelperTest {

    @Mock ResourceInfo resourceInfo;
    @Mock UriInfo uriInfo;
    @Mock FieldHelperMessage fieldHelperMessage;
    @Mock CrudMessage crudMessage;

    MultivaluedMap<String, String> mvmRequest;
    DemoiselleRequestContext drc;
    FieldHelper fieldHelper;

    @BeforeEach
    void setUp() {
        mvmRequest = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        fieldHelper = new FieldHelper(resourceInfo, uriInfo, drc, fieldHelperMessage, crudMessage);
    }

    private void configureForMethod(String methodName) throws Exception {
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod(methodName));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
    }

    @Test
    void requestWithFieldsQueryStringShouldPopulateFields() throws Exception {
        configureForMethod("findWithSearchAndFields");
        mvmRequest.addAll("fields", List.of("id,name"));

        fieldHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getFields());
        assertEquals(2, drc.getFields().getChildren().size());
        assertEquals("id", drc.getFields().getChildren().get(0).getKey());
        assertEquals("name", drc.getFields().getChildren().get(1).getKey());
    }

    @Test
    void requestWithFieldsAndSearchAnnotationShouldValidate() throws Exception {
        configureForMethod("findWithSearchAndFields");

        Search search = UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields").getAnnotation(Search.class);
        StringBuilder sb = new StringBuilder();
        for (String f : search.fields()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(f);
        }
        sb.append(",newInvalidField");
        mvmRequest.addAll("fields", List.of(sb.toString()));

        when(crudMessage.fieldRequestDoesNotExistsOnSearchField("newInvalidField"))
                .thenReturn("Field 'newInvalidField' does not exist on @Search.fields");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnSearchField("newInvalidField");
    }

    @Test
    void requestWithFieldsAndSearchSubFieldsShouldRespectSearchFields() throws Exception {
        configureForMethod("findWithSearchAndFieldsWithSubFields");
        mvmRequest.addAll("fields", List.of("id,name,address"));

        fieldHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getFields());
        assertEquals(3, drc.getFields().getChildren().size());
        assertEquals("id", drc.getFields().getChildren().get(0).getKey());
        assertEquals("name", drc.getFields().getChildren().get(1).getKey());

        TreeNodeField<String, Set<String>> addressNode = drc.getFields().getChildren().get(2);
        assertEquals("address", addressNode.getKey());
        assertEquals(1, addressNode.getChildren().size());
        assertEquals("street", addressNode.getChildren().get(0).getKey());
    }

    @Test
    void requestWithFieldsWithoutSearchAnnotationShouldExecute() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id"));
        mvmRequest.addAll("desc", List.of("id"));
        mvmRequest.putSingle("range", "0-9");
        mvmRequest.addAll("fields", List.of("id", "name"));

        assertDoesNotThrow(() -> fieldHelper.execute(resourceInfo, uriInfo));
        assertNotNull(drc.getFields());
        assertEquals(2, drc.getFields().getChildren().size());
        assertEquals("id", drc.getFields().getChildren().get(0).getKey());
        assertEquals("name", drc.getFields().getChildren().get(1).getKey());
    }

    @Test
    void requestWithFieldsAndSubFieldsShouldParseCorrectly() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("sort", List.of("id"));
        mvmRequest.addAll("desc", List.of("id"));
        mvmRequest.putSingle("range", "0-9");
        mvmRequest.addAll("fields", List.of("id", "name", "address(id,address,country(name))"));

        fieldHelper.execute(resourceInfo, uriInfo);

        assertNotNull(drc.getFields());
        assertEquals(3, drc.getFields().getChildren().size());
        assertEquals("id", drc.getFields().getChildren().get(0).getKey());
        assertEquals("name", drc.getFields().getChildren().get(1).getKey());

        TreeNodeField<String, Set<String>> addressNode = drc.getFields().getChildren().get(2);
        assertEquals("address", addressNode.getKey());
        assertEquals(3, addressNode.getChildren().size());
        assertEquals("id", addressNode.getChildren().get(0).getKey());
        assertEquals("address", addressNode.getChildren().get(1).getKey());
        assertEquals("country", addressNode.getChildren().get(2).getKey());

        TreeNodeField<String, Set<String>> countryNode = addressNode.getChildren().get(2);
        assertEquals("country", countryNode.getKey());
        assertEquals(1, countryNode.getChildren().size());
        assertEquals("name", countryNode.getChildren().get(0).getKey());
    }

    @Test
    void requestWithMalformedFieldsShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("fields", List.of("id", "address(id,address"));

        when(fieldHelperMessage.fieldRequestMalFormed("fields", "address(id,address"))
                .thenReturn("Malformed field request");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(fieldHelperMessage).fieldRequestMalFormed("fields", "address(id,address");
    }

    @Test
    void requestWithInvalidFieldShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("fields", List.of("id1"));

        when(crudMessage.fieldRequestDoesNotExistsOnObject("id1", "org.demoiselle.jee.crud.entity.UserModelForTest"))
                .thenReturn("Field does not exist");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnObject("id1", "org.demoiselle.jee.crud.entity.UserModelForTest");
    }

    @Test
    void requestWithInvalidFieldWithSubFieldShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("fields", List.of("id", "address1(id)"));

        when(crudMessage.fieldRequestDoesNotExistsOnObject("address1", "org.demoiselle.jee.crud.entity.UserModelForTest"))
                .thenReturn("Field does not exist");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnObject("address1", "org.demoiselle.jee.crud.entity.UserModelForTest");
    }

    @Test
    void requestWithInvalidSubFieldShouldThrowIllegalArgumentException() throws Exception {
        configureForMethod("find");
        mvmRequest.addAll("fields", List.of("id", "address(idInvalid)"));

        when(crudMessage.fieldRequestDoesNotExistsOnObject("idInvalid", "org.demoiselle.jee.crud.entity.AddressModelForTest"))
                .thenReturn("Field does not exist");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnObject("idInvalid", "org.demoiselle.jee.crud.entity.AddressModelForTest");
    }

    @Test
    void requestWithFieldSubFieldAndSearchFieldsShouldValidate() throws Exception {
        configureForMethod("findWithSearchAndFieldsWithSubFields");
        mvmRequest.addAll("fields", List.of("id", "address(address)"));

        when(crudMessage.fieldRequestDoesNotExistsOnSearchField("address(address)"))
                .thenReturn("Field does not exist on @Search.fields");

        assertThrows(IllegalArgumentException.class, () -> fieldHelper.execute(resourceInfo, uriInfo));
        verify(crudMessage).fieldRequestDoesNotExistsOnSearchField("address(address)");
    }

    @Test
    void requestWithFieldSubFieldAndSearchWithoutSubFieldShouldAccept() throws Exception {
        configureForMethod("findWithSearch");
        mvmRequest.addAll("fields", List.of("name", "address(address,street)"));

        assertDoesNotThrow(() -> fieldHelper.execute(resourceInfo, uriInfo));
    }

    @Test
    void requestWithFieldsAndSearchAllFieldsShouldAccept() throws Exception {
        configureForMethod("findWithSearchAndAllFields");
        mvmRequest.addAll("fields", List.of("name", "address(address,street)"));

        assertDoesNotThrow(() -> fieldHelper.execute(resourceInfo, uriInfo));
    }
}
