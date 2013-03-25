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
package br.gov.frameworkdemoiselle.internal.bootstrap;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.internal.context.AbstractCustomContext;
import br.gov.frameworkdemoiselle.internal.context.Contexts;
import br.gov.frameworkdemoiselle.internal.context.ViewContext;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, Contexts.class })
public class JsfBootstrapTest {

	private JsfBootstrap bootstrap;

	private AfterBeanDiscovery event;

	@Before
	public void before() {
		event = createMock(AfterBeanDiscovery.class);
		mockStatic(Beans.class);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault()).anyTimes();
		replay(Beans.class);
		bootstrap = new JsfBootstrap();
	}

	@Test
	public void testStoreContexts() {
		bootstrap.storeContexts(event);
		replay(event);

		Assert.assertEquals(event, Whitebox.getInternalState(bootstrap, "afterBeanDiscoveryEvent"));
		List<AbstractCustomContext> context = Whitebox.getInternalState(bootstrap, "tempContexts");
		Assert.assertEquals(1, context.size());
		verifyAll();
	}

	@Test
	public void testAddContexts() {
		List<AbstractCustomContext> tempContexts = new ArrayList<AbstractCustomContext>();
		AbstractCustomContext tempContext = new ViewContext();
		tempContexts.add(tempContext);
		Whitebox.setInternalState(bootstrap, "tempContexts", tempContexts);
		Whitebox.setInternalState(bootstrap, "afterBeanDiscoveryEvent", event);

		AfterDeploymentValidation afterDeploymentValidation = createMock(AfterDeploymentValidation.class);

		event.addContext(tempContext);

		replay(event, afterDeploymentValidation);

		bootstrap.addContexts(afterDeploymentValidation);
		verifyAll();
	}

	@Test
	public void testRemoveContexts() {
		bootstrap.storeContexts(event);

		AfterShutdownProccess afterShutdownProccess = createMock(AfterShutdownProccess.class);
		replay(event, afterShutdownProccess);
		bootstrap.removeContexts(afterShutdownProccess);

		verifyAll();
	}

}
