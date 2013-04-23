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
package exception;

import javax.inject.Inject;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class OneExceptionTest {

	@Inject
	private OneException oneException;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(OneExceptionTest.class);
		return deployment;
	}

	@Test
	public void testExceptionWithHandler() {
		oneException.throwExceptionWithHandler();
		assertEquals(true, oneException.isExceptionHandler());
	}

	@Test
	public void testExceptionWithoutHandler() {
		try {
			oneException.throwExceptionWithoutHandler();
			fail();
		} catch (Exception cause) {
			assertEquals(ArithmeticException.class, cause.getClass());
		}
	}

	@Test
	public void testExceptionWithMultiHandler() {
		oneException.throwIllegalArgumentException();
		assertEquals(false, oneException.isExceptionHandlerIllegalArgument1());
		assertEquals(true, oneException.isExceptionHandlerIllegalArgument2());
		assertEquals(false, oneException.isExceptionHandlerIllegalArgument3());
	}

	@Test
	public void testExceptionHandlerWithException() {
		try {
			oneException.throwNoSuchElementException();
		} catch (Exception e) {
			assertEquals(ArithmeticException.class, e.getClass());
		}

	}
}
