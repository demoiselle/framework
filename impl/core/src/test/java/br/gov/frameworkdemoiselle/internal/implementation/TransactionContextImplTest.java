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
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.internal.bootstrap.TransactionBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.TransactionConfig;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Beans.class,StrategySelector.class})
public class TransactionContextImplTest {

	private TransactionContext context;	
	private Transaction transaction;
	
	@Test
	public void testGetTransactionNull() {
		context = new TransactionContextImpl();
		
		Class<? extends Transaction> cache = TransactionImpl.class;					
		
		List<Class<? extends Transaction>> cacheList = new ArrayList<Class<? extends Transaction>>();
		cacheList.add(cache);
		
		TransactionBootstrap bootstrap = PowerMock.createMock(TransactionBootstrap.class);
		TransactionConfig config = PowerMock.createMock(TransactionConfig.class);
		
		mockStatic(Beans.class); 
		expect(Beans.getReference(TransactionBootstrap.class)).andReturn(bootstrap).anyTimes();
		expect(Beans.getReference(TransactionConfig.class)).andReturn(config);
		expect(config.getTransactionClass()).andReturn(null).anyTimes();	
		expect(bootstrap.getCache()).andReturn(cacheList);
		expect(Beans.getReference(TransactionImpl.class)).andReturn(new TransactionImpl());
		
		replayAll(Beans.class);
		
		transaction = context.getCurrentTransaction();
		Assert.assertEquals(transaction.getClass(),TransactionImpl.class);
	}
	
	class TransactionImpl implements Transaction{
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public boolean isActive() {
			return false;
		}
		
		@Override
		public boolean isMarkedRollback() {
			return false;
		}
		
		@Override
		public void begin() {
		}
		
		@Override
		public void commit() {
		}
		
		@Override
		public void rollback() {
		}
		
		@Override
		public void setRollbackOnly() {
		}
	}
	
}
