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
package exception.basic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.DemoiselleException;

@RunWith(Arquillian.class)
public class ExceptionHandlerTest {

	@Inject
	private SimpleExceptionHandler exceptionHandler;

	@Inject
	private MultiExceptionHandler multiExceptionHandler;

	@Inject
	private ExceptionHandlerTwoParameter exceptionTwoParameter;

	@Inject
	private ExceptionNested nested;
	
	@Inject
	private ExceptionClassNotAnnotated classNotAnnotated;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(ExceptionHandlerTest.class);
		return deployment;
	}

	@Test
	public void exceptionWithHandler() {
		exceptionHandler.throwArithmeticException();
		exceptionHandler.throwNullPointerException();
		assertEquals(true, exceptionHandler.isArithmeticExceptionHandler());
		assertEquals(true, exceptionHandler.isArithmeticExceptionHandler());
	}

	@Test
	public void exceptionWithoutHandler() {
		try {
			exceptionHandler.throwExceptionWithoutHandler();
			fail();
		} catch (Exception cause) {
			assertEquals(IllegalArgumentException.class, cause.getClass());
		}
	}

	@Test
	public void twoExceptionOneMethod() {
		exceptionHandler.throwTwoException();
		assertEquals(true, exceptionHandler.isNullPointerExceptionHandler());
		assertEquals(false, exceptionHandler.isArithmeticExceptionHandler());
	}

	@Test
	public void exceptionWithMultiHandler() {
		multiExceptionHandler.throwIllegalArgumentException();
		assertEquals(false, multiExceptionHandler.isExceptionHandlerIllegalArgument1());
		assertEquals(true, multiExceptionHandler.isExceptionHandlerIllegalArgument2());
		assertEquals(false, multiExceptionHandler.isExceptionHandlerIllegalArgument3());
	}

	@Test
	public void exceptionHandlerWithTwoParameter() {
		try {
			exceptionTwoParameter.throwIllegalPathException();
			fail();
		} catch (Exception e) {
			assertEquals(DemoiselleException.class, e.getClass());
		}
	}

	@Test
	public void exceptionHandlerWithException() {
		try {
			nested.throwNoSuchElementException();
			fail();
		} catch (Exception e) {
			assertEquals(ArithmeticException.class, e.getClass());
			assertEquals(false, nested.isExceptionHandler());
		}
	}
	
	@Test
	public void exceptionClassNotAnnotatedController() {
		try {
			classNotAnnotated.throwNullPointerException();
			fail();
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
		}
	}
}
