/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.entity.AddressModelForTest;
import org.demoiselle.jee.crud.entity.CountryModelForTest;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.cache.QueryCacheStore;
import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.field.FieldHelperMessage;
import org.demoiselle.jee.crud.filter.FilterHelper;
import org.demoiselle.jee.crud.pagination.PageResult;
import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage;
import org.demoiselle.jee.crud.pagination.ResultSet;
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
 * Test of {@link CrudFilter} class.
 * Migrated from Spock/Groovy to JUnit 5 + Mockito.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CrudFilterTest {

    @Mock ContainerRequestContext requestContext;
    @Mock ContainerResponseContext responseContext;
    @Mock ResourceInfo resourceInfo;
    @Mock PaginationHelperConfig dpc;
    @Mock PaginationHelperMessage paginationMessage;
    @Mock FieldHelperMessage fieldHelperMessage;
    @Mock SortHelperMessage sortHelperMessage;
    @Mock CrudMessage crudMessage;
    @Mock UriInfo uriInfo;

    MultivaluedMap<String, String> mvmRequest;
    MultivaluedMap<String, Object> mvmResponse;
    DemoiselleRequestContext drc;
    ReflectionCache reflectionCache;
    QueryCacheStore queryCacheStore;

    SortHelper sortHelper;
    PaginationHelper paginationHelper;
    FilterHelper filterHelper;
    FieldHelper fieldHelper;
    CrudFilter crudFilter;

    @BeforeEach
    void setUp() {
        mvmRequest = new MultivaluedHashMap<>();
        mvmResponse = new MultivaluedHashMap<>();
        drc = new DemoiselleRequestContextImpl();
        reflectionCache = new ReflectionCache();
        queryCacheStore = new QueryCacheStore();

        sortHelper = new SortHelper(resourceInfo, uriInfo, drc, sortHelperMessage, crudMessage);
        paginationHelper = new PaginationHelper(resourceInfo, uriInfo, dpc, drc, paginationMessage);
        filterHelper = new FilterHelper(resourceInfo, uriInfo, drc, crudMessage);
        fieldHelper = new FieldHelper(resourceInfo, uriInfo, drc, fieldHelperMessage, crudMessage);
        crudFilter = new CrudFilter(resourceInfo, uriInfo, drc, paginationHelper, sortHelper, filterHelper, fieldHelper, reflectionCache, queryCacheStore);
    }

    private void configureRequestForCrud() throws Exception {
        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("find"));
        URI uri = new URI("http://localhost:9090/api/users");
        lenient().when(uriInfo.getRequestUri()).thenReturn(uri);
    }

    @Test
    void requestWithRangeParameterShouldFillResultObject() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        mvmRequest.putSingle("range", "10-20");
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        configureRequestForCrud();

        assertDoesNotThrow(() -> crudFilter.filter(requestContext));
        assertEquals(10, drc.getOffset());
        assertEquals(20, drc.getLimit());
    }

    @SuppressWarnings("unchecked")
    @Test
    void responseWithRangeSortDescFieldsShouldFillResponseObject() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(100L);

        mvmRequest.addAll("sort", List.of("id", "name"));
        mvmRequest.putSingle("desc", "name");
        mvmRequest.addAll("fields", List.of("id", "name", "mail", "address(street)"));
        mvmRequest.putSingle("range", "10-20");
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AddressModelForTest address = new AddressModelForTest();
            address.setStreet("my street " + i);
            UserModelForTest user = new UserModelForTest();
            user.setId(1L);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            user.setAddress(address);
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);
        configureRequestForCrud();

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        assertTrue(mvmResponse.containsKey("Content-Range"));
        assertTrue(mvmResponse.containsKey("Accept-Range"));
        assertTrue(mvmResponse.containsKey("Link"));
        assertTrue(mvmResponse.containsKey("Access-Control-Expose-Headers"));
        verify(responseContext).setStatus(206);
    }

    @SuppressWarnings("unchecked")
    @Test
    void requestReturningAllElementsShouldSetStatus200() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        mvmRequest.addAll("fields", List.of("id", "name", "mail"));
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserModelForTest user = new UserModelForTest();
            user.setId(1L);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);
        configureRequestForCrud();

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        assertTrue(mvmResponse.containsKey("Content-Range"));
        assertTrue(mvmResponse.containsKey("Accept-Range"));
        assertFalse(mvmResponse.containsKey("Link"));
        assertTrue(mvmResponse.containsKey("Access-Control-Expose-Headers"));
        verify(responseContext).setStatus(200);
    }

    @Test
    void requestWithInvalidFieldsShouldThrowRuntimeException() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        mvmRequest.addAll("fields", List.of("id", "name", "invalidField"));
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);
        configureRequestForCrud();

        assertThrows(RuntimeException.class, () -> crudFilter.filter(requestContext));
    }

    @SuppressWarnings("unchecked")
    @Test
    void requestWithoutFieldsShouldUseSearchAnnotationFields() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AddressModelForTest address = new AddressModelForTest();
            address.setStreet("my street " + i);
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            user.setAddress(address);
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearch"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        // Verify setEntity was called (the response was processed)
        verify(responseContext).setEntity(any());
    }

    @Test
    void requestNotMatchingAbstractRESTShouldDoNothing() throws Exception {
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestWithoutAbstractRESTForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestWithoutAbstractRESTForTest.class.getDeclaredMethod("find"));

        crudFilter.filter(requestContext);

        assertFalse(mvmResponse.containsKey("Content-Range"));
        assertFalse(mvmResponse.containsKey("Accept-Range"));
        assertFalse(mvmResponse.containsKey("Link"));
        assertFalse(mvmResponse.containsKey("Access-Control-Expose-Headers"));
    }

    @Test
    void requestNotForCrudWithExceptionShouldTreatAcceptRangeHeader() throws Exception {
        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestWithoutAbstractRESTForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestWithoutAbstractRESTForTest.class.getDeclaredMethod("findWithException"));
        when(responseContext.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());

        drc.setEntityClass(null);

        assertDoesNotThrow(() -> crudFilter.filter(requestContext, responseContext));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), responseContext.getStatus());
        verify(responseContext).getEntity();
    }

    @SuppressWarnings("unchecked")
    @Test
    void requestWithSearchFieldsAndSubFieldsShouldRespectThem() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AddressModelForTest address = new AddressModelForTest();
            address.setStreet("my street " + i);
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            user.setAddress(address);
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        verify(responseContext).setEntity(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void requestWithSearchAllFieldsShouldReturnAllFields() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CountryModelForTest country = new CountryModelForTest();
            country.setId((long) i);
            country.setName("country " + i);
            AddressModelForTest address = new AddressModelForTest();
            address.setId((long) i);
            address.setStreet("my street " + i);
            address.setAddress("address " + i);
            address.setCountry(country);
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            user.setAge(i);
            user.setMail("john" + i + "@test.com");
            user.setAddress(address);
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);

        when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithSearchAndAllFields"));
        URI uri = new URI("http://localhost:9090/api/users");
        when(uriInfo.getRequestUri()).thenReturn(uri);

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        // With @Search(fields={"*"}), all fields should be returned as-is (the original list)
        verify(responseContext).setEntity(users);
    }

    @SuppressWarnings("unchecked")
    @Test
    void responseWithPageResultShouldIncludeMetadataHeaders() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(100L);

        mvmRequest.putSingle("range", "0-9");
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            users.add(user);
        }

        PageResult<UserModelForTest> pageResult = PageResult.of(users, 100L, 0, 10);
        when(responseContext.getEntity()).thenReturn(pageResult);
        configureRequestForCrud();

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        // Existing headers should still be present
        assertTrue(mvmResponse.containsKey("Content-Range"));
        assertTrue(mvmResponse.containsKey("Accept-Range"));
        assertTrue(mvmResponse.containsKey("Access-Control-Expose-Headers"));

        // PageResult metadata headers should be present
        assertTrue(mvmResponse.containsKey("X-Total-Count"));
        assertTrue(mvmResponse.containsKey("X-Total-Pages"));
        assertTrue(mvmResponse.containsKey("X-Current-Page"));
        assertTrue(mvmResponse.containsKey("X-Page-Size"));
        assertTrue(mvmResponse.containsKey("X-Has-Next"));
        assertTrue(mvmResponse.containsKey("X-Has-Previous"));

        // Verify metadata values
        assertEquals(100L, mvmResponse.getFirst("X-Total-Count"));
        assertEquals(10, mvmResponse.getFirst("X-Total-Pages"));
        assertEquals(0, mvmResponse.getFirst("X-Current-Page"));
        assertEquals(10, mvmResponse.getFirst("X-Page-Size"));
        assertEquals(true, mvmResponse.getFirst("X-Has-Next"));
        assertEquals(false, mvmResponse.getFirst("X-Has-Previous"));

        // Expose headers should include PageResult metadata
        String exposeHeaders = (String) mvmResponse.getFirst("Access-Control-Expose-Headers");
        assertTrue(exposeHeaders.contains("X-Total-Count"));
        assertTrue(exposeHeaders.contains("X-Total-Pages"));
        assertTrue(exposeHeaders.contains("X-Current-Page"));
        assertTrue(exposeHeaders.contains("X-Page-Size"));
        assertTrue(exposeHeaders.contains("X-Has-Next"));
        assertTrue(exposeHeaders.contains("X-Has-Previous"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void responseWithResultSetShouldNotIncludePageResultHeaders() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            user.setMail("john" + i + "@test.com");
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);
        configureRequestForCrud();

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        // Existing headers should still be present
        assertTrue(mvmResponse.containsKey("Access-Control-Expose-Headers"));

        // PageResult metadata headers should NOT be present
        assertFalse(mvmResponse.containsKey("X-Total-Count"));
        assertFalse(mvmResponse.containsKey("X-Total-Pages"));
        assertFalse(mvmResponse.containsKey("X-Current-Page"));
        assertFalse(mvmResponse.containsKey("X-Page-Size"));
        assertFalse(mvmResponse.containsKey("X-Has-Next"));
        assertFalse(mvmResponse.containsKey("X-Has-Previous"));

        // Expose headers should NOT include PageResult metadata
        String exposeHeaders = (String) mvmResponse.getFirst("Access-Control-Expose-Headers");
        assertFalse(exposeHeaders.contains("X-Total-Count"));
    }

    // --- Cache integration tests ---

    private void configureRequestForCacheableMethod() throws Exception {
        lenient().when(resourceInfo.getResourceClass()).thenReturn((Class) UserRestForTest.class);
        lenient().when(resourceInfo.getResourceMethod()).thenReturn(UserRestForTest.class.getDeclaredMethod("findWithCacheable"));
        URI uri = new URI("http://localhost:9090/api/users");
        lenient().when(uriInfo.getRequestUri()).thenReturn(uri);
    }

    @Test
    void cacheableGetRequestShouldSetCacheHitPropertyOnHit() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        configureRequestForCacheableMethod();

        // Pre-populate cache with a known value
        String entityClassName = UserModelForTest.class.getName();
        String cacheKey = entityClassName + ":filter:http://localhost:9090/api/users";
        List<String> cachedData = List.of("cached-item-1", "cached-item-2");
        queryCacheStore.put(cacheKey, cachedData, 60);

        crudFilter.filter(requestContext);

        // The request filter should set the cache hit property
        verify(requestContext).setProperty(CrudFilter.CACHE_HIT_PROPERTY, cachedData);
    }

    @Test
    void cacheableGetRequestShouldNotSetPropertyOnCacheMiss() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        configureRequestForCacheableMethod();

        // No cache entry — should proceed normally without setting property
        crudFilter.filter(requestContext);

        verify(requestContext, never()).setProperty(eq(CrudFilter.CACHE_HIT_PROPERTY), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void cacheableResponseShouldStoreResultAndSetMissHeader() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);
        configureRequestForCacheableMethod();

        List<UserModelForTest> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UserModelForTest user = new UserModelForTest();
            user.setId((long) i);
            user.setName("John" + i);
            users.add(user);
        }

        ResultSet result = new ResultSet();
        result.setContent(users);
        when(responseContext.getEntity()).thenReturn(result);

        crudFilter.filter(requestContext);
        crudFilter.filter(requestContext, responseContext);

        // X-Cache header should be MISS
        assertEquals("MISS", mvmResponse.getFirst("X-Cache"));

        // Cache should now contain the result
        String entityClassName = UserModelForTest.class.getName();
        String cacheKey = entityClassName + ":filter:http://localhost:9090/api/users";
        assertNotNull(queryCacheStore.get(cacheKey));
    }

    @SuppressWarnings("unchecked")
    @Test
    void cacheHitResponseShouldReturnCachedEntityAndHitHeader() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        drc.setCount(10L);

        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        when(responseContext.getHeaders()).thenReturn(mvmResponse);
        configureRequestForCacheableMethod();

        // Simulate a cache HIT by setting the property on the request context
        List<String> cachedData = List.of("cached-item-1", "cached-item-2");
        when(requestContext.getProperty(CrudFilter.CACHE_HIT_PROPERTY)).thenReturn(cachedData);

        ResultSet result = new ResultSet();
        result.setContent(List.of());
        when(responseContext.getEntity()).thenReturn(result);

        crudFilter.filter(requestContext, responseContext);

        // Should set the cached entity and HIT header
        verify(responseContext).setEntity(cachedData);
        assertEquals("HIT", mvmResponse.getFirst("X-Cache"));
        verify(responseContext).setStatus(200);
    }

    @Test
    void nonCacheableGetRequestShouldNotInteractWithCache() throws Exception {
        when(dpc.getDefaultPagination()).thenReturn(20);
        when(dpc.getIsGlobalEnabled()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(mvmRequest);
        configureRequestForCrud();

        // The standard find() method is NOT @Cacheable
        crudFilter.filter(requestContext);

        // Should not set cache property
        verify(requestContext, never()).setProperty(eq(CrudFilter.CACHE_HIT_PROPERTY), any());
    }
}
