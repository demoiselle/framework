/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.demoiselle.jee.crud.batch.BatchConfig;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AbstractDAO.persistAll() (Task 11.2).
 *
 * Validates: Requirements 4.1, 4.4, 4.7
 */
class PersistAllTest {

    static class TestDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;
        TestDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    private EntityManager em;
    private TestDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        em = mock(EntityManager.class);
        dao = new TestDAO(em);

        // Inject BatchConfig via reflection
        BatchConfig batchConfig = new BatchConfig();
        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, batchConfig);
    }

    @Test
    void persistAll_emptyList_returnsEmptyList() {
        List<UserModelForTest> result = dao.persistAll(List.of());

        assertTrue(result.isEmpty());
        // flush/clear still called at the end
        verify(em).flush();
        verify(em).clear();
    }

    @Test
    void persistAll_allEntitiesPersisted_returnsAllEntities() {
        List<UserModelForTest> entities = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserModelForTest u = new UserModelForTest();
            u.setName("user" + i);
            entities.add(u);
        }

        List<UserModelForTest> result = dao.persistAll(entities);

        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertSame(entities.get(i), result.get(i));
            verify(em).persist(entities.get(i));
        }
    }

    @Test
    void persistAll_flushesAndClearsEveryBatchSize() throws Exception {
        // Set batch size to 2 via reflection
        BatchConfig smallBatch = new BatchConfig();
        Field sizeField = BatchConfig.class.getDeclaredField("size");
        sizeField.setAccessible(true);
        sizeField.setInt(smallBatch, 2);

        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, smallBatch);

        List<UserModelForTest> entities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            entities.add(new UserModelForTest());
        }

        dao.persistAll(entities);

        // With batch size 2 and 5 entities:
        // flush/clear at index 1 (i+1=2), index 3 (i+1=4), then final flush/clear
        // Total: 3 flushes, 3 clears
        verify(em, times(3)).flush();
        verify(em, times(3)).clear();
    }

    @Test
    void persistAll_persistenceException_throwsDemoiselleCrudExceptionWithIndex() {
        List<UserModelForTest> entities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            entities.add(new UserModelForTest());
        }

        PersistenceException cause = new PersistenceException("DB error");
        // Fail on the 3rd entity (index 2)
        doNothing().when(em).persist(entities.get(0));
        doNothing().when(em).persist(entities.get(1));
        doThrow(cause).when(em).persist(entities.get(2));

        DemoiselleCrudException ex = assertThrows(DemoiselleCrudException.class,
                () -> dao.persistAll(entities));

        assertTrue(ex.getMessage().contains("índice 2"));
        assertSame(cause, ex.getCause());
    }

    @Test
    void persistAll_exactBatchSize_flushesOnce() throws Exception {
        // Set batch size to 3
        BatchConfig config = new BatchConfig();
        Field sizeField = BatchConfig.class.getDeclaredField("size");
        sizeField.setAccessible(true);
        sizeField.setInt(config, 3);

        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, config);

        List<UserModelForTest> entities = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            entities.add(new UserModelForTest());
        }

        dao.persistAll(entities);

        // flush/clear at index 2 (i+1=3), then final flush/clear
        // Total: 2 flushes, 2 clears
        verify(em, times(2)).flush();
        verify(em, times(2)).clear();
    }
}
