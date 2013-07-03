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
package br.gov.frameworkdemoiselle.internal.implementation;

import static br.gov.frameworkdemoiselle.annotation.Priority.L2_PRIORITY;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.ClassUtils;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.configuration.ConfigurationValueExtractor;

@Priority(L2_PRIORITY)
public class ConfigurationPrimitiveOrWrapperValueExtractor implements ConfigurationValueExtractor {

	private static final Set<Object> wrappers = new HashSet<Object>();

	static {
		wrappers.add(Boolean.class);
		wrappers.add(Byte.class);
		wrappers.add(Character.class);
		wrappers.add(Short.class);
		wrappers.add(Integer.class);
		wrappers.add(Long.class);
		wrappers.add(Double.class);
		wrappers.add(Float.class);
		wrappers.add(Void.TYPE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		Object value;

		try {
			value = new DataConfiguration(configuration).get(ClassUtils.primitiveToWrapper(field.getType()), prefix
					+ key);

		} catch (ConversionException cause) {
			throw cause;
		}

		return value;
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType().isPrimitive() || wrappers.contains(field.getType());
	}
}
