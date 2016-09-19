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
package org.demoiselle.configuration;

import java.lang.reflect.Field;
import org.apache.commons.configuration2.Configuration;

/**
 * <p>
 * Interface that defines how to convert values extracted from configuration
 * files to fields in a class annotated with {@link Configuration}.
 * </p>
 *
 * <p>
 * Primitive types like <code>int</code> and <code>float</code>, their wrapper
 * counterparts like {@link Integer} and {@link Float} and the {@link String} class
 * can already be converted by the framework, this interface is reserved for specialized
 * classes.
 * </p>
 * 
 * @author SERPRO
 */
@SuppressWarnings("unused")
public interface ConfigurationValueExtractor {

	/**
	 * Method that must appropriately extract the value from a property file and set this value to a 
	 * field in a configuration class.
	 * 
	 * @param prefix
	 * 			optional parte of property name that must be concatenated with <b>key</b> to form the whole 
	 * 			property name.
	 * @param key
	 * 			key of the property.
	 * @param field
	 * 			configuration field to be setted.
	 * @param configuration
	 * 			a configuration object.
	 * @return current value of this property
	 * @throws Exception if the value can't be extracted from the property file
	 */
	Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception;

	/**
	 * Checks if the extractor class is appropriate to extract values to the type of deffined by parameter
	 * <b>field</b>.
	 * 
	 * @param field
	 * 			field to be checked.
	 * @return <code>true</code> if this extractor can convert this field into the extractor's final type
	 */
	boolean isSupported(Field field);
}
