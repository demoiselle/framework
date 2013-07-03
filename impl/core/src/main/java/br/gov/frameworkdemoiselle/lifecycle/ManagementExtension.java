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
package br.gov.frameworkdemoiselle.lifecycle;

import java.util.List;

import br.gov.frameworkdemoiselle.internal.management.ManagedType;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;

/**
 * <p>
 * Interface defining the lifecycle of a <b>management extension</b>, an extension capable of exposing
 * {@link ManagementController}'s to external clients in one of the available management technologies, such as JMX or
 * SNMP.
 * </p>
 * <p>
 * To include a management extension into the management lifecycle, it just needs to implement this interface and be a
 * CDI bean (have a <b>beans.xml</b> file inside the META-INF folder of it's java package). The Demoiselle Core
 * lifecycle controller will call the {@link #initialize(List managedTypes)} and {@link #shutdown(List managedTypes)}
 * methods at the apropriate times.
 * </p>
 * 
 * @author serpro
 */
public interface ManagementExtension {

	/**
	 * This method is called during the application initialization process for each concrete implementation of this
	 * interface.
	 * 
	 * @param managedTypes
	 *            The list of discovered {@link ManagementController} classes.
	 */
	void initialize(List<ManagedType> managedTypes);

	/**
	 * This method is called during the application shutdown process for each concrete implementation of this interface.
	 * 
	 * @param managedTypes
	 *            The list of discovered {@link ManagementController} classes.
	 */
	void shutdown(List<ManagedType> managedTypes);

}
