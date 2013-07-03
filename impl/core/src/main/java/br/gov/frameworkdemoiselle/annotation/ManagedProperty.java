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
package br.gov.frameworkdemoiselle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * <p>
 * Indicates that a field must be exposed as a property to management clients.
 * </p>
 * <p>
 * The property will be writable if there's a public setter method declared for the field and readable if there's a
 * getter method. You can override this behaviour by passing a value to the {@link #accessLevel()} property.
 * </p>
 * <p>
 * It's a runtime error to annotate a field with no getter and no setter method.
 * </p>
 * <p>
 * It's also a runtime error to declare a field as a property and one or both of it's getter and setter methods as an
 * operation using the {@link ManagedOperation} annotation.
 * </p>
 * 
 * @author SERPRO
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagedProperty {

	/**
	 * @return The description of this property exposed to management clients.
	 */
	@Nonbinding
	String description() default "";

	@Nonbinding
	ManagedPropertyAccess accessLevel() default ManagedPropertyAccess.DEFAULT;

	/**
	 * <p>
	 * Access level of a managed property.
	 * </p>
	 * <p>
	 * These values only affect access via external management clients, the application is still able to inject and use
	 * the normal visibility of the property by Java standards.
	 * </p>
	 * 
	 * @author serpro
	 */
	enum ManagedPropertyAccess {

		/**
		 * Restricts a property to be only readable even if a setter method exists.
		 */
		READ_ONLY

		/**
		 * Restricts a property to be only writeable even if a getter method exists.
		 */
		, WRITE_ONLY

		/**
		 * Says that the read or write access will be determined by the presence of a getter method
		 * <code>(getProperty())</code> or setter method <code>(setProperty(propertyValue))</code> for a property.
		 */
		, DEFAULT;
	}

}
