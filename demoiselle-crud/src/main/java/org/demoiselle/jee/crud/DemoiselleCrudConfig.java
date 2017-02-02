/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 *
 * @author SERPRO
 */
//TODO CLF revisar
@Configuration(prefix = "demoiselle.crud")
public class DemoiselleCrudConfig {

    private boolean patternsEnabled = true;
    private int acceptRange = 20;

    public int getAcceptRange() {
        return acceptRange;
    }

    public void setAcceptRange(int acceptRange) {
        this.acceptRange = acceptRange;
    }

    public boolean isPatternsEnabled() {
        return patternsEnabled;
    }

    public void setPatternsEnabled(boolean patternsEnabled) {
        this.patternsEnabled = patternsEnabled;
    }

}
