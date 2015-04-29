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
package management.domain;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.management.ManagedOperation;
import br.gov.frameworkdemoiselle.management.ManagedProperty;
import br.gov.frameworkdemoiselle.management.OperationParameter;
import br.gov.frameworkdemoiselle.management.OperationType;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;

/**
 * Classe usada para testar se o registro de classes Managed
 * como MBeans está funcionando.
 * 
 * @author SERPRO
 *
 */
@ManagementController
@Name("ManagedTest")
public class ManagedTestClass {
	
	@ManagedProperty(description="Atributo de teste para testar registro de MBean")
	private String attribute;

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	@ManagedOperation(type=OperationType.ACTION_INFO,description="Test Operation")
	public String operation(@OperationParameter(name="parameter") String parameter){
		return "Operation called with parameter="+parameter+". Current attribute value is "+attribute;
	}
	
}
