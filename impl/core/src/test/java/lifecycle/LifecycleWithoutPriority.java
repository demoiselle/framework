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
package lifecycle;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import br.gov.frameworkdemoiselle.lifecycle.Shutdown;
import br.gov.frameworkdemoiselle.lifecycle.Startup;

@ApplicationScoped
public class LifecycleWithoutPriority {

	private List<Integer> priorityStartup = new ArrayList<Integer>();

	private List<Integer> priorityShutdown = new ArrayList<Integer>();

	public List<Integer> getPriorityStartup() {
		return priorityStartup;
	}

	public List<Integer> getPriorityShutdown() {
		return priorityShutdown;
	}

	@Startup
	public void loadWithoutPriorityFirst() {
		priorityStartup.add(1);
	}

	@Startup
	public void loadWithoutPrioritySecond() {
		priorityStartup.add(3);
	}

	@Startup
	public void loadWithoutPriorityThird() {
		priorityStartup.add(2);
	}

	@Shutdown
	public void unloadWithoutPriorityFirst() {
		priorityShutdown.add(3);
	}

	@Shutdown
	public void unloadWithoutPrioritySecond() {
		priorityShutdown.add(2);
	}

	@Shutdown
	public void unloadWithoutPriorityThird() {
		priorityShutdown.add(1);
	}

}
