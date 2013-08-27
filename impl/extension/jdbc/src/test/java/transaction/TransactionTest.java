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

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.transaction.JDBCTransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@RunWith(Arquillian.class)
public class TransactionTest {

	private static String PATH = "src/test/resources/transaction";

	@Inject
	private TransactionalBusiness tb;

	private Transaction transaction;

	@Inject
	private TransactionContext context;

	@Inject
	private DDL ddl;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(TransactionTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}

	@Before
	public void init() throws Exception {
		transaction = context.getCurrentTransaction();
		ddl.dropAndCreate();
		transaction.commit();
	}

	@Test
	public void isTransactionActiveWithInterceptor() {
		Assert.assertTrue(tb.isTransactionActiveWithInterceptor());
	}

	@Test
	public void isTransactionActiveWithoutInterceptor() {
		Assert.assertFalse(tb.isTransactionActiveWithoutInterceptor());
	}

	@Test
	public void verifyIfTransactionIsJdbcTransaction() {
		Assert.assertEquals(transaction.getClass(), JDBCTransaction.class);
	}

	@Test
	public void verifyIfTransactionIsActive() {
		Assert.assertTrue(!transaction.isActive());
		transaction.begin();
		Assert.assertTrue(transaction.isActive());
	}
	
	@Test
	public void commitWithSuccess() throws Exception{
		MyEntity m = new MyEntity();
		m.setId(1);
		m.setDescription("desc-1");
		
		tb.insert(m);

		Assert.assertEquals("desc-1", tb.find(m.getId()).getDescription());
		
		tb.delete(m);
		
		Assert.assertNull(tb.find(m.getId()).getDescription());
	}	

	@Test
	public void rollbackWithSuccess() throws Exception {
		try{
			tb.rollbackWithSuccess();
		} catch (Exception e) {
			Assert.assertEquals("Exceção criada para marcar transação para rollback", e.getMessage());
		}
		finally{
			MyEntity m = tb.find(3);
			Assert.assertNull(tb.find(m.getId()).getDescription());
		}
	}	
	
	@Test(expected=SQLException.class)
	public void closedConnection() throws Exception{
		MyEntity m = new MyEntity();
		m.setId(1);
		m.setDescription("desc-1");
		
		tb.insertWithouTransaction(m);
		
		Connection conn = Beans.getReference(Connection.class, new NameQualifier("conn"));
		conn.close();
		
		tb.find(m.getId());
	}		
}
