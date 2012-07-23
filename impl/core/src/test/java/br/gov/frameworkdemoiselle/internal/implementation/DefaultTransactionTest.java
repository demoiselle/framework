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
package br.gov.frameworkdemoiselle.internal.implementation;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author SERPRO
 * @see DefaultTransaction
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBundle.class)
public class DefaultTransactionTest {

	private DefaultTransaction tx;

	@Before
	public void setUp() throws Exception {
		tx = new DefaultTransaction();

		mockStatic(CoreBundle.class);

		ResourceBundle bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		expect(CoreBundle.get()).andReturn(bundle);

		replay(CoreBundle.class);
	}

	@After
	public void tearDown() {
		tx = null;
	}

	@Test
	public void testBegin() {
		try {
			tx.begin();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testCommit() {
		try {
			tx.commit();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testIsActive() {
		try {
			tx.isActive();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testIsMarkedRollback() {
		try {
			tx.isMarkedRollback();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testRollback() {
		try {
			tx.rollback();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testSetRollbackOnly() {
		try {
			tx.setRollbackOnly();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}
}
