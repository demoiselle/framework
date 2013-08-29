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
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;

public class TransactionalBusiness {

	@Inject
	private Connection conn;

	@Inject
	private TransactionContext transactionContext;

	@Transactional
	public boolean isTransactionActiveWithInterceptor() {
		return transactionContext.getCurrentTransaction().isActive();
	}

	public boolean isTransactionActiveWithoutInterceptor() {
		return transactionContext.getCurrentTransaction().isActive();
	}

	@Transactional
	public void insert(MyEntity m) throws Exception {
		String sql = "insert into myentity (id, description) values (" + m.getId() + ", '" + m.getDescription() + "')";
		Statement st = conn.createStatement();
		st.executeUpdate(sql);
		st.close();
	}

	public void insertWithouTransaction(MyEntity m) throws Exception {
		String sql = "insert into myentity (id, description) values (" + m.getId() + ", '" + m.getDescription() + "')";
		Statement st = conn.createStatement();
		st.executeUpdate(sql);
		st.close();
	}

	@Transactional
	public void delete(MyEntity m1) throws Exception {
		String sql = "delete from myentity where id = " + m1.getId();
		Statement st = conn.createStatement();
		st.executeUpdate(sql);
		st.close();
	}

	public MyEntity find(int id) throws Exception {
		String sql = "select * from myentity where id = " + id;
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);

		MyEntity m = new MyEntity();
		while (rs.next()) {
			m.setId(rs.getInt(1));
			m.setDescription(rs.getString(2));
		}

		rs.close();
		st.close();

		return m;
	}

	@Transactional
	public void rollbackWithSuccess() throws Exception {
		MyEntity m = new MyEntity();
		m.setId(3);

		this.insert(m);

		throw new Exception("Exceção criada para marcar transação para rollback");
	}
}
