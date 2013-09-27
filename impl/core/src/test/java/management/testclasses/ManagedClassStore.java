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
package management.testclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import br.gov.frameworkdemoiselle.internal.implementation.ManagedType;
import br.gov.frameworkdemoiselle.internal.implementation.Management;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Bean class that stores managed types detected by the management bootstrap
 * and can read/write properties and invoke operations on them, simulating a management
 * extension like JMX or SNMP. 
 * 
 * @author SERPRO
 *
 */
@ApplicationScoped
public class ManagedClassStore {
	
	private List<ManagedType> managedTypes = new ArrayList<ManagedType>();

	
	public List<ManagedType> getManagedTypes() {
		return managedTypes;
	}

	public void addManagedTypes(Collection<ManagedType> managedTypes){
		this.managedTypes.addAll(managedTypes);
	}
	
	public void setProperty(Class<?> managedClass , String attributeName , Object newValue){
		Management manager = Beans.getReference(Management.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				manager.setProperty(type, attributeName, newValue);
				break;
			}
		}
	}
	
	public Object getProperty(Class<?> managedClass , String attributeName ){
		Management manager = Beans.getReference(Management.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				return manager.getProperty(type, attributeName);
			}
		}
		
		return null;
	}
	
	public Object invoke(Class<?> managedClass , String operation , Object...  params){
		Management manager = Beans.getReference(Management.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				return manager.invoke(type, operation, params);
			}
		}
		
		return null;
	}
}
