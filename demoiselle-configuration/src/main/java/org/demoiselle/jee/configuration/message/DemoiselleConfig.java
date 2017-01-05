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
package org.demoiselle.jee.configuration.message;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.jee")
public class DemoiselleConfig {
	
	private String product;

    private String build;

    public String getBuild() {
        return build;
    }

	public String getProduct() {
		return product;
	}
    
    
}
