/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package org.demoiselle.internal.implementation;



import org.apache.commons.configuration2.Configuration;
import org.demoiselle.annotation.Priority;
import org.demoiselle.configuration.ConfigurationValueExtractor;

import javax.enterprise.context.Dependent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.demoiselle.annotation.Priority.L2_PRIORITY;

@SuppressWarnings("unused")
@Priority(L2_PRIORITY)
@Dependent
public class ConfigurationMapValueExtractor implements ConfigurationValueExtractor {

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		Map<String, Object> value = null;

		String regexp = "^(" + prefix + ")(" + key + ")(\\.(\\w+))?$";
		Pattern pattern = Pattern.compile(regexp);

		for (Iterator<String> iter = configuration.getKeys(); iter.hasNext();) {
			String iterKey = iter.next();
			Matcher matcher = pattern.matcher(iterKey);

			if (matcher.matches()) {
				String confKey = matcher.group(1) + matcher.group(2) + ( matcher.group(3)!=null ? matcher.group(3) : "" );
						
						/*matcher.group(1) + (matcher.group(2) == null ? "" : matcher.group(2))
						+ matcher.group(4);*/

				if (value == null) {
					value = new HashMap<>();
				}

				String mapKey = matcher.group(4) == null ? "default" : matcher.group(4);
				value.put(mapKey, configuration.getString(confKey));
			}
		}

		return value;
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType() == Map.class;
	}
}
