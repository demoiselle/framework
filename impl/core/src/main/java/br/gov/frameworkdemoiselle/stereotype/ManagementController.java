/*
 * Demoiselle Framework
 * Copyright (C) 2011 SERPRO
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
package br.gov.frameworkdemoiselle.stereotype;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.Nonbinding;

/**
 * <p>
 * Identifies a <b>management controller</b> bean. What it means is that an external client can manage the application
 * this class is in by reading or writing it's attributes and calling it's operations.
 * </p>
 * <p>
 * Only fields annotated with {@link br.gov.frameworkdemoiselle.management.ManagedProperty} or methods annotated with
 * {@link br.gov.frameworkdemoiselle.management.ManagedOperation} will be exposed to clients.
 * </p>
 * <p>
 * Only bean implementations (concrete classes) can be management controllers. It's a runtime error to mark an interface
 * or abstract class with this annotation.
 * </p>
 * <p>
 * This stereotype only defines a class as managed, you need to choose an extension that will expose this managed class
 * to external clients using any technology available. One example is the Demoiselle JMX extension, that will expose
 * managed classes as MBeans.
 * </p>
 * 
 * @author SERPRO
 */
@ApplicationScoped
@Stereotype
@Documented
@Controller
@Inherited
@Retention(RUNTIME)
@Target({ TYPE })
public @interface ManagementController {

	/**
	 * @return Human readable description of this managed class.
	 */
	@Nonbinding
	String description() default "";

}
