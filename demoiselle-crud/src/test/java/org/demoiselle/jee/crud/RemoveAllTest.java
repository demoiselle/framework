/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;

import org.demoiselle.jee.crud.batch.BatchConfig;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AbstractDAO.removeAll() (Task 11.3).
 *
 * Validates: Requirements 4.2, 4.5, 4.8
 */
class RemoveAllTest {

    static class TestDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;
        int removeCallCount = 0;

        TestDAO(EntityManager em) { this.em = em; }

        @Override
        protected EntityManager getEntityManager() { return em; }

        @Override
        public void remove(Long id) {
            removeCallCount++;
            // Simulate normal remove (no-op for unit test)
        }
    }

    private EntityManager em;
    private TestDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        em = mock(EntityManager.class);
        dao = new TestDAO(em);

        BatchConfig batchConfig = new BatchConfig();
        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, batchConfig);
    }

    @Test
    void removeAll_emptyList_returnsZero() {
        int result = dao.removeAll(List.of());

        assertEquals(0, result);
        assertEquals(0, dao.removeCallCount);
        verify(em).flush();
        verify(em).clear();
    }

    @Test
    void removeAll_delegatesToRemoveForEachId() {
        List<Long> ids = List.of(1L, 2L, 3L);

        int result = dao.removeAll(ids);

        assertEquals(3, result);
        assertEquals(3, dao.removeCallCount);
    }

    @Test
    void removeAll_flushesAndClearsEveryBatchSize() throws Exception {
        // Set batch size to 2
        BatchConfig smallBatch = new BatchConfig();
        Field sizeField = BatchConfig.class.getDeclaredField("size");
        sizeField.setAccessible(true);
        sizeField.setInt(smallBatch, 2);

        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, smallBatch);

        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L);

        dao.removeAll(ids);

        // batch size 2, 5 ids:
        // flush/clear at i=1 (i+1=2), i=3 (i+1=4), then final flush/clear
        // Total: 3 flushes, 3 clears
        verify(em, times(3)).flush();
        verify(em, times(3)).clear();
    }

    @Test
    void removeAll_exactBatchSize_flushesAtBoundaryAndEnd() throws Exception {
        // Set batch size to 3
        BatchConfig config = new BatchConfig();
        Field sizeField = BatchConfig.class.getDeclaredField("size");
        sizeField.setAccessible(true);
        sizeField.setInt(config, 3);

        Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
        batchField.setAccessible(true);
        batchField.set(dao, config);

        List<Long> ids = List.of(10L, 20L, 30L);

        int result = dao.removeAll(ids);

        assertEquals(3, result);
        // flush/clear at i=2 (i+1=3), then final flush/clear
        // Total: 2 flushes, 2 clears
        verify(em, times(2)).flush();
        verify(em, times(2)).clear();
    }

    @Test
    void removeAll_returnsCountEqualToListSize() {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 7; i++) {
            ids.add(i);
        }

        int result = dao.removeAll(ids);

        assertEquals(7, result);
        assertEquals(7, dao.removeCallCount);
    }
}
