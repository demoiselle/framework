/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.batch;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchConfigTest {

    @Test
    void defaultSizeIsFifty() {
        assertEquals(50, new BatchConfig().getSize());
    }

    @Test
    void zeroSizeIsRejectedWithClearConfigurationError() throws Exception {
        BatchConfig config = withSize(0);

        assertThrows(IllegalStateException.class, config::getSize);
    }

    @Test
    void negativeSizeIsRejectedWithClearConfigurationError() throws Exception {
        BatchConfig config = withSize(-10);

        assertThrows(IllegalStateException.class, config::getSize);
    }

    private BatchConfig withSize(int size) throws Exception {
        BatchConfig config = new BatchConfig();
        Field field = BatchConfig.class.getDeclaredField("size");
        field.setAccessible(true);
        field.setInt(config, size);
        return config;
    }
}
