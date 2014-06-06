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
package pagination;

import static junit.framework.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import transaction.defaultstrategy.TransactionDefaultTest;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.internal.configuration.PaginationConfig;
import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class PaginationContextBasicTest {

	private static final int VALID_PAGE_SIZE = 5;

	private static final int VALID_CURRENT_PAGE = 2;

	private static final int VALID_TOTAL_RESULTS = 30;

	private static final int VALID_FIRST_RESULT = 5;

	private static final int INVALID_PAGE_SIZE = -5;

	private static final int INVALID_NEGATIVE_CURRENT_PAGE = -2;

	private static final int INVALID_CURRENT_PAGE = 21;

	private static final int INVALID_TOTAL_RESULTS = -1;

	private static final int INVALID_FIRST_RESULT = VALID_TOTAL_RESULTS + 1;

	private static final int INVALID_NEGATIVE_FIRST_RESULT = -19;

	@Inject
	private PaginationContext paginationContext;

	@Inject
	private PaginationConfig paginationConfig;

	private Pagination pagination;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(TransactionDefaultTest.class);
		return deployment;
	}

	@Before
	public void activeContext() {
		// SessionContext context = Beans.getReference(SessionContext.class);
		RequestContext context = Beans.getReference(RequestContext.class);
		context.activate();
		pagination = paginationContext.getPagination(DummyEntity.class, true);
	}

	@After
	public void deactiveContext() {
		// SessionContext context = Beans.getReference(SessionContext.class);
		RequestContext context = Beans.getReference(RequestContext.class);
		context.deactivate();
	}

	@Test
	public void defaultPaginationContext() {
		assertEquals(pagination.getPageSize(), paginationConfig.getPageSize());
		assertEquals(pagination.getCurrentPage(), 0);
		assertEquals(pagination.getTotalResults(), 0);
		assertEquals(pagination.getTotalPages(),
				(int) Math.ceil(pagination.getTotalResults() * 1d / pagination.getPageSize()));
		assertEquals(pagination.getFirstResult(), pagination.getCurrentPage() * pagination.getPageSize());
	}

	@Test
	public void changeToValidValuesAttributes() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setCurrentPage(VALID_CURRENT_PAGE);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);
		assertEquals(pagination.getPageSize(), VALID_PAGE_SIZE);
		assertEquals(pagination.getCurrentPage(), VALID_CURRENT_PAGE);
		assertEquals(pagination.getTotalResults(), VALID_TOTAL_RESULTS);
		assertEquals(pagination.getTotalPages(), (int) Math.ceil(VALID_TOTAL_RESULTS * 1d / VALID_PAGE_SIZE));
		assertEquals(pagination.getFirstResult(), VALID_CURRENT_PAGE * VALID_PAGE_SIZE);

		pagination.setFirstResult(VALID_FIRST_RESULT);
		assertEquals(pagination.getFirstResult(), VALID_FIRST_RESULT);
	}

	@Test
	public void currentPageGreaterThanTotalPages() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setCurrentPage((int) Math.ceil(VALID_TOTAL_RESULTS * 1d / VALID_PAGE_SIZE) + 1);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);

		assertEquals(pagination.getCurrentPage(), (int) Math.ceil(VALID_TOTAL_RESULTS * 1d / VALID_PAGE_SIZE) - 1);
	}

	@Test
	public void resetCurrentAndTotalPagesWithTotalResults() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setCurrentPage(VALID_CURRENT_PAGE);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);

		assertEquals(pagination.getCurrentPage(), VALID_CURRENT_PAGE);
		assertEquals(pagination.getTotalPages(),
				(int) Math.ceil(pagination.getTotalResults() * 1d / pagination.getPageSize()));

		pagination.setTotalResults(0);

		assertEquals(pagination.getCurrentPage(), 0);
		assertEquals(pagination.getTotalPages(), 0);
	}

	@Test
	public void resetCurrentAndTotalPagesWithPageSize() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setCurrentPage(VALID_CURRENT_PAGE);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);

		assertEquals(pagination.getCurrentPage(), VALID_CURRENT_PAGE);
		assertEquals(pagination.getTotalPages(),
				(int) Math.ceil(pagination.getTotalResults() * 1d / pagination.getPageSize()));

		pagination.setPageSize(0);

		assertEquals(pagination.getCurrentPage(), 0);
		assertEquals(pagination.getTotalPages(), 0);
	}

	@Test
	public void resetCurrentPageWithFirstResult() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setCurrentPage(VALID_CURRENT_PAGE);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);

		pagination.setFirstResult(0);

		assertEquals(pagination.getCurrentPage(), 0);
	}

	@Test
	public void paginationToString() {
		assertEquals("PaginationImpl [currentPage=0, pageSize=10, totalResults=0, totalPages=0]", pagination.toString());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changeCurrentPageToInvalidNegativeValuesAttributes() {
		pagination.setCurrentPage(INVALID_NEGATIVE_CURRENT_PAGE);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changeCurrentPageToInvalidValuesAttributes() {
		pagination.setPageSize(VALID_PAGE_SIZE);
		pagination.setTotalResults(VALID_TOTAL_RESULTS);

		pagination.setCurrentPage(INVALID_CURRENT_PAGE);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changePageSizeToInvalidValuesAttributes() {
		pagination.setPageSize(INVALID_PAGE_SIZE);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changeTotalResultsToInvalidValuesAttributes() {
		pagination.setTotalResults(INVALID_TOTAL_RESULTS);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changeFirstResultToInvalidNegativeValuesAttributes() {
		pagination.setFirstResult(INVALID_NEGATIVE_FIRST_RESULT);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void changeFirstResultToInvalidValuesAttributes() {
		pagination.setTotalResults(VALID_TOTAL_RESULTS);
		pagination.setFirstResult(INVALID_FIRST_RESULT);
	}

}
