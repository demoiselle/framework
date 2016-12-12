/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.core.annotation.Priority;

@Dependent
@Priority(Priority.L1_PRIORITY)
public class ConfigurationStringValueExtractorAmbiguosTest implements ConfigurationValueExtractor{

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return null;
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType() == String.class;
	}

	
}
