/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import java.io.IOException;

import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.After;

/**
 * 
 * @author SERPRO
 *
 */
public abstract class AbstractConfigurationTest {

    protected String FILE_PREFIX = "app";
    protected String PREFIX = "";
    protected UtilTest utilTest = new UtilTest();

    @After
    public void destroy() throws IOException {
        utilTest.deleteFilesAfterTest();
    }

}
