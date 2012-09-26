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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * @author SERPRO
 */

public class PaginationImplTest {

	private Pagination pagination;

	@Before
	public void setUp() {
		pagination = new PaginationImpl();
	}

	@After
	public void tearDown() {
		pagination = null;
	}

	@Test
	public void testConstructor() {
		assertEquals(0, pagination.getCurrentPage());
		assertEquals(0, pagination.getTotalResults());
		assertEquals(0, pagination.getTotalPages());
		assertEquals(0, pagination.getFirstResult());
	}

	@Test
	public void testCurrentPageProperty() {
		pagination.setCurrentPage(0);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setCurrentPage(1);
		assertEquals(1, pagination.getCurrentPage());
	}

	@Test
	public void testPageSizeProperty() {
		pagination.setPageSize(0);
		assertEquals(0, pagination.getPageSize());

		pagination.setPageSize(1);
		assertEquals(1, pagination.getPageSize());
	}

	@Test
	public void testTotalResultsProperty() {
		pagination.setTotalResults(0);
		assertEquals(0, pagination.getTotalResults());

		pagination.setTotalResults(1);
		assertEquals(1, pagination.getTotalResults());
	}

	@Test
	public void testTotalPagesWhenTotalResultsChanges() {
		pagination.setPageSize(10);

		pagination.setTotalResults(0);
		assertEquals(0, pagination.getTotalPages());

		pagination.setTotalResults(9);
		assertEquals(1, pagination.getTotalPages());

		pagination.setTotalResults(10);
		assertEquals(1, pagination.getTotalPages());

		pagination.setTotalResults(11);
		assertEquals(2, pagination.getTotalPages());
	}

	@Test
	public void testIndexOutOfBoundsException() {
		try {
			pagination.setCurrentPage(-1);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setFirstResult(-1);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setPageSize(-1);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setTotalResults(-1);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setTotalResults(1);
			pagination.setFirstResult(1);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setTotalResults(1);
			pagination.setFirstResult(2);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setPageSize(2);
			pagination.setTotalResults(3);
			pagination.setCurrentPage(2);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setPageSize(2);
			pagination.setTotalResults(3);
			pagination.setCurrentPage(3);
			fail();
		} catch (IndexOutOfBoundsException cause) {
		}

		try {
			pagination.setTotalResults(0);
			pagination.setFirstResult(0);
		} catch (IndexOutOfBoundsException cause) {
			fail();
		}
	}

	@Test
	public void testTotalPagesWhenPageSizeChanges() {
		pagination.setTotalResults(10);

		pagination.setPageSize(0);
		assertEquals(0, pagination.getTotalPages());

		pagination.setPageSize(9);
		assertEquals(2, pagination.getTotalPages());

		pagination.setPageSize(10);
		assertEquals(1, pagination.getTotalPages());

		pagination.setPageSize(11);
		assertEquals(1, pagination.getTotalPages());
	}

	@Test
	public void testCurrentPageWhenPageSizeChanges() {
		pagination.setTotalResults(10);

		pagination.setPageSize(5);
		pagination.setCurrentPage(1);
		pagination.setPageSize(0);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setPageSize(5);
		pagination.setCurrentPage(1);
		pagination.setPageSize(9);
		assertEquals(1, pagination.getCurrentPage());

		pagination.setPageSize(5);
		pagination.setCurrentPage(1);
		pagination.setPageSize(10);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setPageSize(5);
		pagination.setCurrentPage(0);
		pagination.setPageSize(11);
		assertEquals(0, pagination.getCurrentPage());
	}

	@Test
	public void testCurrentPageWhenTotalResultsChanges() {
		pagination.setPageSize(10);

		pagination.setTotalResults(11);
		pagination.setCurrentPage(1);
		pagination.setTotalResults(0);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setTotalResults(11);
		pagination.setCurrentPage(1);
		pagination.setTotalResults(9);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setTotalResults(11);
		pagination.setCurrentPage(1);
		pagination.setTotalResults(10);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setTotalResults(12);
		pagination.setCurrentPage(1);
		pagination.setTotalResults(11);
		assertEquals(1, pagination.getCurrentPage());
	}

	@Test
	public void testCurrentPageWhenFirstResultChanges() {
		pagination.setPageSize(10);
		pagination.setTotalResults(100);

		pagination.setFirstResult(0);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setFirstResult(1);
		assertEquals(0, pagination.getCurrentPage());

		pagination.setFirstResult(98);
		assertEquals(9, pagination.getCurrentPage());

		pagination.setFirstResult(99);
		assertEquals(9, pagination.getCurrentPage());
	}

	@Test
	public void testFirstResultWhenPageSizeChanges() {
		pagination.setTotalResults(10);

		pagination.setPageSize(10);
		pagination.setFirstResult(5);
		pagination.setPageSize(0);
		assertEquals(0, pagination.getFirstResult());

		pagination.setPageSize(10);
		pagination.setFirstResult(9);
		pagination.setPageSize(1);
		assertEquals(0, pagination.getFirstResult());
	}

	@Test
	public void testFirstResultWhenTotalResultsChanges() {
		pagination.setPageSize(10);

		pagination.setTotalResults(50);
		pagination.setFirstResult(49);
		pagination.setTotalResults(0);
		assertEquals(0, pagination.getFirstResult());

		pagination.setTotalResults(50);
		pagination.setFirstResult(49);
		pagination.setTotalResults(1);
		assertEquals(0, pagination.getFirstResult());

		pagination.setTotalResults(50);
		pagination.setFirstResult(49);
		pagination.setTotalResults(49);
		assertEquals(40, pagination.getFirstResult());
	}

	@Test
	public void testFirstResultWhenCurrentPageChanges() {
		pagination.setPageSize(10);
		pagination.setTotalResults(100);

		pagination.setCurrentPage(0);
		assertEquals(0, pagination.getFirstResult());

		pagination.setCurrentPage(1);
		assertEquals(10, pagination.getFirstResult());

		pagination.setCurrentPage(9);
		assertEquals(90, pagination.getFirstResult());
	}

	@Test
	public void testToStringFormat() {
		assertEquals(Strings.toString(pagination), pagination.toString());
	}
}
