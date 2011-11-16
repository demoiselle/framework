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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies a method eligible to be executed automatically during <b>application finalization</b>.
 * <p>
 * Take a look at the following usage sample:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * public class Finalizer {
 * 
 * 	&#064;Shutdown(priority = 5)
 *    public void finalize() {
 *       ...
 *    }
 * }
 * 
 * 
 * 
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The <code>@Shutdown</code> annotation allows an integer value to be defined, which stands for the method execution
 * priority when several finalizer classes are available in the application.
 * 
 * @author SERPRO
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Shutdown {

	/**
	 * Most important priority value.
	 */
	public static int MAX_PRIORITY = Integer.MIN_VALUE;

	/**
	 * Less important priority value.
	 */
	public static int MIN_PRIORITY = Integer.MAX_VALUE;

	/**
	 * An integer value defines method execution order (i.e., priority).
	 */
	int priority() default MIN_PRIORITY;

}
