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
package br.gov.frameworkdemoiselle.configuration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.Nonbinding;

/**
 * Identifies a <b>configuration class</b>, that is, a structure reserved to store configuration values retrieved from a
 * given resource file or system variables.
 * <p>
 * This class is gonna have a single instance throughout the application, as stated by the <b>singleton</b> design
 * pattern approach.
 * <p>
 * A <i>Configuration</i> is:
 * <ul>
 * <li>defined when annotated with {@code @Configuration}</li>
 * <li>automatically injected whenever {@code @Inject} is used</li>
 * </ul>
 * 
 * @author SERPRO
 */
// @Singleton
@Stereotype
@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {

	/**
	 * Define the default resource.
	 */
	public static final String DEFAULT_RESOURCE = "demoiselle";

	/**
	 * Defines the resource type to be used: a properties file, an XML file, or system variables.
	 * <p>
	 * If not specified, a properties resource file is to be considered.
	 * 
	 * @return ConfigType
	 */
	@Nonbinding
	ConfigType type() default ConfigType.PROPERTIES;

	/**
	 * Defines an optional prefix to be used on every parameter key.
	 * <p>
	 * For instance, if prefix is set to <code>"frameworkdemoiselle.pagination"</code> and an attribute named
	 * <code>defaultPageSize</code> is found in the class, the corresponding key
	 * <code>frameworkdemoiselle.pagination.defaultPageSize</code> is expected to be read in the resource file.
	 * 
	 * @return String
	 */
	@Nonbinding
	String prefix() default "";

	/**
	 * Defines the resource file name to be read by this configuration class. There is no need to specify file extension
	 * in the case of properties or XML resources.
	 * <p>
	 * For instance, when resource is set to <code>"bookmark"</code> and the type set to properties, a corresponding
	 * file named <code>bookmark.properties</code> is considered.
	 * <p>
	 * If not specified, the default configuration file <code>demoiselle.properties</code> is rather considered.
	 * 
	 * @return String
	 */
	@Nonbinding
	String resource() default DEFAULT_RESOURCE;

}
