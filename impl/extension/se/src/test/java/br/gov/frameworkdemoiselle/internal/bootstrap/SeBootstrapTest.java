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

import java.util.List;
import java.util.Locale;

import javax.enterprise.inject.spi.AfterBeanDiscovery;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.internal.context.AbstractCustomContext;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, ContextManager.class })
@Ignore
public class SeBootstrapTest {

	private SeBootstrap seBootstrap;

	private AfterBeanDiscovery event;

	@Before
	public void before() {
		event = createMock(AfterBeanDiscovery.class);
		mockStatic(Beans.class);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault()).anyTimes();
		replay(Beans.class);
		seBootstrap = new SeBootstrap();
	}

	@Test
	public void testStoreContext() {
		seBootstrap.storeContexts(event);
		replay(event);

		Assert.assertEquals(event, Whitebox.getInternalState(seBootstrap, "afterBeanDiscoveryEvent"));
		List<AbstractCustomContext> context = Whitebox.getInternalState(seBootstrap, "tempContexts");
		Assert.assertEquals(4, context.size());
		verifyAll();
	}

	@Test
	public void testRemoveContexts() {
		seBootstrap.storeContexts(event);

		AfterShutdownProccess afterShutdownProccess = createMock(AfterShutdownProccess.class);
		replay(event, afterShutdownProccess);
		seBootstrap.removeContexts(afterShutdownProccess);

		verifyAll();
	}
}
