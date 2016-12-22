/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * TODO javadoc
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.rest")
public class DemoiselleRestConfig {

    private boolean gzipEnabled = false;

    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public void setGzipEnabled(boolean gzipEnabled) {
        this.gzipEnabled = gzipEnabled;
    }

}
