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
package br.gov.frameworkdemoiselle.internal.proxy;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import br.gov.frameworkdemoiselle.internal.producer.ConnectionProducer;
import br.gov.frameworkdemoiselle.util.Beans;

public class ConnectionProxy implements Connection, Serializable {

	private static final long serialVersionUID = 1L;

	private final String dataSourceName;

	public ConnectionProxy(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	private Connection getDelegate() {
		ConnectionProducer emp = Beans.getReference(ConnectionProducer.class);
		return emp.getConnection(this.dataSourceName);
	}

	public void clearWarnings() throws SQLException {
		getDelegate().clearWarnings();
	}

	public void close() throws SQLException {
		getDelegate().close();
	}

	public void commit() throws SQLException {
		getDelegate().commit();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return getDelegate().createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return getDelegate().createBlob();
	}

	public Clob createClob() throws SQLException {
		return getDelegate().createClob();
	}

	public NClob createNClob() throws SQLException {
		return getDelegate().createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return getDelegate().createSQLXML();
	}

	public Statement createStatement() throws SQLException {
		return getDelegate().createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return getDelegate().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return getDelegate().createStatement(resultSetType, resultSetConcurrency);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return getDelegate().createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException {
		return getDelegate().getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return getDelegate().getCatalog();
	}

	public Properties getClientInfo() throws SQLException {
		return getDelegate().getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		return getDelegate().getClientInfo(name);
	}

	public int getHoldability() throws SQLException {
		return getDelegate().getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return getDelegate().getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return getDelegate().getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return getDelegate().getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return getDelegate().getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return getDelegate().isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return getDelegate().isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException {
		return getDelegate().isValid(timeout);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getDelegate().isWrapperFor(iface);
	}

	public String nativeSQL(String sql) throws SQLException {
		return getDelegate().nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return getDelegate().prepareCall(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return getDelegate().prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return getDelegate().prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return getDelegate().prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getDelegate().prepareStatement(sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		getDelegate().releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		getDelegate().rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		getDelegate().rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		getDelegate().setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		getDelegate().setCatalog(catalog);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		getDelegate().setClientInfo(properties);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		getDelegate().setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException {
		getDelegate().setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		getDelegate().setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return getDelegate().setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return getDelegate().setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		getDelegate().setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		getDelegate().setTypeMap(map);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getDelegate().unwrap(iface);
	}

	@Override
	public void abort(Executor executor) throws SQLException {
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return getDelegate().getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return getDelegate().getSchema();
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		getDelegate().setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		getDelegate().setSchema(schema);
	}
}
