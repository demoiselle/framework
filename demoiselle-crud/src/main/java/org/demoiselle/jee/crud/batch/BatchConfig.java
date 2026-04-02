/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.batch;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Class responsible for holding configuration about Batch operations feature.
 * 
 * <p>
 * The batch size can be configured via the property
 * {@code demoiselle.crud.batch.size} in the demoiselle.properties file.
 * Default value is 50.
 * </p>
 */
@Configuration(prefix = "demoiselle.crud.batch")
public class BatchConfig {

    private int size = 50;

    public int getSize() {
        return size;
    }

}
