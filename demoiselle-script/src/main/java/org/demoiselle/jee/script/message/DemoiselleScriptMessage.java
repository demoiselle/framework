/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 *
 * @author SERPRO
 */
@MessageBundle
public interface DemoiselleScriptMessage {
					   
	@MessageTemplate("{error-engine-cannot-load}")
    String cannotLoadEngine(String engineName);

    @MessageTemplate("{error-engine-not-loaded}")
    String engineNotLoaded();
        
    @MessageTemplate("{error-engine-not-compilable}")
    String engineNotCompilable();
    
    @MessageTemplate("{error-script-not-loaded}")
    String scriptNotLoaded(String scriptName);

}
