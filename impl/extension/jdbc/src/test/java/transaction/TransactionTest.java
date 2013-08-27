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

package transaction;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.internal.context.ManagedContext;

@RunWith(Arquillian.class)
public class TransactionTest {

	private static String PATH = "src/test/resources/transaction";

	@Inject
	private TransactionalBusiness tb;
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(TransactionTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}
	
//	@Before
//	public void init() throws Exception{
////		transaction = context.getCurrentTransaction();
////		ddl.dropAndCreate();
//	}
	
//	@Before
//	public void activeContext() {
//		ContextManager.activate(ManagedContext.class, RequestScoped.class);
//	}
//
//	@After
//	public void deactiveContext() {
//		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
//	}
	
	@Test
	public void isTransactionActiveWithInterceptor(){
		Assert.assertTrue(tb.isTransactionActiveWithInterceptor());
	}
	
	@Test
	public void isTransactionActiveWithoutInterceptor(){
		Assert.assertFalse(tb.isTransactionActiveWithoutInterceptor());
	}

//	@Test
//	public void verifyIfTransactionIsJdbcTransaction() {
//		assertEquals(transaction.getClass(), JDBCTransaction.class);
//	}
//	
//	@Test
//	public void verifyIfTransactionIsActive() {
//		assertTrue(!transaction.isActive());
//		transaction.begin();
//		assertTrue(transaction.isActive());
//	}
	
	
}
	