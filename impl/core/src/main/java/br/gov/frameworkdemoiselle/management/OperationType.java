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
package br.gov.frameworkdemoiselle.management;


/**
 * <p>
 * Define the operation type for an operation inside a ManagementController class.
 * </p>
 * <p>
 * This is an optional annotation and it's significanse will change based on the management extension used. Most
 * extensions will just publish this information to the client so it can better show to the user the inner workings of
 * the annotated operation.
 * </p>
 * 
 * @author SERPRO
 */
public enum OperationType {

	/**
	 * ManagedOperation is write-only, it causes the application to change some of it's behaviour but doesn't return any
	 * kind of information
	 */
	ACTION,

	/**
	 * ManagedOperation is read-only, it will operate over data provided by the application and return some information,
	 * but will not change the application in any way.
	 */
	INFO,
	
	/**
	 * ManagedOperation is read-write, it will both change the way the application work and return some information
	 * regarding the result of the operation.
	 */
	ACTION_INFO,

	/**
	 * The effect of calling this operation is unknown. This is the default type and if this type is assigned to an
	 * operation, the user must rely on the {@link ManagedOperation#description()} attribute to learn about how the
	 * operation works.
	 */
	UNKNOWN
}
