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
package br.gov.frameworkdemoiselle.util;

import br.gov.frameworkdemoiselle.exception.ApplicationException;

/**
 *Class that offer tow methods that can help with manipulation of throwable exceptions. 
 * 
 * @author SERPRO
 */
public final class Exceptions {

	/**
	 * Constructor without parameters.
	 */
	private Exceptions() {
	}

	/**
	 * Informs if a Throwable class is annotated with @ApplicationException or not.
	 * 
	 * @param throwable
	 * 			a throwable object.
	 *      
	 * @return a boolean, true if thowable is annotated with @ApplicationException, and false otherwise.          
	 */
	public static boolean isApplicationException(final Throwable throwable) {
		return throwable != null && throwable.getClass().isAnnotationPresent(ApplicationException.class);
	}

	/**
	 * Receives as parameter any kind of Throwable objects, and throws a RuntimeException instead. 
	 * 
	 * @param throwable
	 * 			a throwable object.
	 *    
	 * @throws RuntimeException throws this kind of exception every time that is called.             
	 */
	public static void handleToRuntimeException(final Throwable throwable) throws RuntimeException {
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		} else {
			throw new RuntimeException(throwable);
		}
	}
}
