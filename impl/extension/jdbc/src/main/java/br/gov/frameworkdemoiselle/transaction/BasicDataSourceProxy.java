package br.gov.frameworkdemoiselle.transaction;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.dbcp.BasicDataSource;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class BasicDataSourceProxy extends BasicDataSource implements Serializable {

	private static final long serialVersionUID = 1L;

	private ResourceBundle bundle;

	private String dataSourceName;

	private JDBCConfig config;

	private transient BasicDataSource delegate;

	public BasicDataSourceProxy(String dataSourceName, JDBCConfig config, ResourceBundle bundle) {
		this.dataSourceName = dataSourceName;
		this.config = config;
		this.bundle = bundle;
	}

	private BasicDataSource getDelegate() {
		if (this.delegate == null) {
			BasicDataSource dataSource = new BasicDataSource();

			try {
				String driver = config.getDriverClass().get(dataSourceName);
				String url = config.getUrl().get(dataSourceName);
				String username = config.getUsername().get(dataSourceName);
				String password = config.getPassword().get(dataSourceName);

				dataSource.setDriverClassName(driver);
				dataSource.setUrl(url);
				dataSource.setUsername(username);
				dataSource.setPassword(password);

			} catch (ClassCastException cause) {
				throw new DemoiselleException(bundle.getString("load-duplicated-configuration-failed"), cause);
			}

			delegate = dataSource;
		}

		return this.delegate;
	}

	public boolean getDefaultAutoCommit() {
		return getDelegate().getDefaultAutoCommit();
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		getDelegate().setDefaultAutoCommit(defaultAutoCommit);
	}

	public boolean getDefaultReadOnly() {
		return getDelegate().getDefaultReadOnly();
	}

	public void setDefaultReadOnly(boolean defaultReadOnly) {
		getDelegate().setDefaultReadOnly(defaultReadOnly);
	}

	public int getDefaultTransactionIsolation() {
		return getDelegate().getDefaultTransactionIsolation();
	}

	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		getDelegate().setDefaultTransactionIsolation(defaultTransactionIsolation);
	}

	public String getDefaultCatalog() {
		return getDelegate().getDefaultCatalog();
	}

	public void setDefaultCatalog(String defaultCatalog) {
		getDelegate().setDefaultCatalog(defaultCatalog);
	}

	public String getDriverClassName() {
		return getDelegate().getDriverClassName();
	}

	public void setDriverClassName(String driverClassName) {
		getDelegate().setDriverClassName(driverClassName);
	}

	public ClassLoader getDriverClassLoader() {
		return getDelegate().getDriverClassLoader();
	}

	public void setDriverClassLoader(ClassLoader driverClassLoader) {
		getDelegate().setDriverClassLoader(driverClassLoader);
	}

	public int getMaxActive() {
		return getDelegate().getMaxActive();
	}

	public void setMaxActive(int maxActive) {
		getDelegate().setMaxActive(maxActive);
	}

	public int getMaxIdle() {
		return getDelegate().getMaxIdle();
	}

	public void setMaxIdle(int maxIdle) {
		getDelegate().setMaxIdle(maxIdle);
	}

	public int getMinIdle() {
		return getDelegate().getMinIdle();
	}

	public void setMinIdle(int minIdle) {
		getDelegate().setMinIdle(minIdle);
	}

	public int getInitialSize() {
		return getDelegate().getInitialSize();
	}

	public void setInitialSize(int initialSize) {
		getDelegate().setInitialSize(initialSize);
	}

	public long getMaxWait() {
		return getDelegate().getMaxWait();
	}

	public void setMaxWait(long maxWait) {
		getDelegate().setMaxWait(maxWait);
	}

	public boolean isPoolPreparedStatements() {
		return getDelegate().isPoolPreparedStatements();
	}

	public void setPoolPreparedStatements(boolean poolingStatements) {
		getDelegate().setPoolPreparedStatements(poolingStatements);
	}

	public int getMaxOpenPreparedStatements() {
		return getDelegate().getMaxOpenPreparedStatements();
	}

	public void setMaxOpenPreparedStatements(int maxOpenStatements) {
		getDelegate().setMaxOpenPreparedStatements(maxOpenStatements);
	}

	public boolean getTestOnBorrow() {
		return getDelegate().getTestOnBorrow();
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		getDelegate().setTestOnBorrow(testOnBorrow);
	}

	public boolean getTestOnReturn() {
		return getDelegate().getTestOnReturn();
	}

	public void setTestOnReturn(boolean testOnReturn) {
		getDelegate().setTestOnReturn(testOnReturn);
	}

	public long getTimeBetweenEvictionRunsMillis() {
		return getDelegate().getTimeBetweenEvictionRunsMillis();
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		getDelegate().setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	public int getNumTestsPerEvictionRun() {
		return getDelegate().getNumTestsPerEvictionRun();
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		getDelegate().setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	public long getMinEvictableIdleTimeMillis() {
		return getDelegate().getMinEvictableIdleTimeMillis();
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		getDelegate().setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	public boolean getTestWhileIdle() {
		return getDelegate().getTestWhileIdle();
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		getDelegate().setTestWhileIdle(testWhileIdle);
	}

	public int getNumActive() {
		return getDelegate().getNumActive();
	}

	public int getNumIdle() {
		return getDelegate().getNumIdle();
	}

	public String getPassword() {
		return getDelegate().getPassword();
	}

	public void setPassword(String password) {
		getDelegate().setPassword(password);
	}

	public String getUrl() {
		return getDelegate().getUrl();
	}

	public void setUrl(String url) {
		getDelegate().setUrl(url);
	}

	public String getUsername() {
		return getDelegate().getUsername();
	}

	public void setUsername(String username) {
		getDelegate().setUsername(username);
	}

	public String getValidationQuery() {
		return getDelegate().getValidationQuery();
	}

	public void setValidationQuery(String validationQuery) {
		getDelegate().setValidationQuery(validationQuery);
	}

	public int getValidationQueryTimeout() {
		return getDelegate().getValidationQueryTimeout();
	}

	public void setValidationQueryTimeout(int timeout) {
		getDelegate().setValidationQueryTimeout(timeout);
	}

	@SuppressWarnings("rawtypes")
	public Collection getConnectionInitSqls() {
		return getDelegate().getConnectionInitSqls();
	}

	@SuppressWarnings("rawtypes")
	public void setConnectionInitSqls(Collection connectionInitSqls) {
		getDelegate().setConnectionInitSqls(connectionInitSqls);
	}

	public void setAccessToUnderlyingConnectionAllowed(boolean allow) {
		getDelegate().setAccessToUnderlyingConnectionAllowed(allow);
	}

	public Connection getConnection(String user, String pass) throws SQLException {
		return getDelegate().getConnection(user, pass);
	}

	public int getLoginTimeout() throws SQLException {
		return getDelegate().getLoginTimeout();
	}

	public PrintWriter getLogWriter() throws SQLException {
		return getDelegate().getLogWriter();
	}

	public void setLoginTimeout(int loginTimeout) throws SQLException {
		getDelegate().setLoginTimeout(loginTimeout);
	}

	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		getDelegate().setLogWriter(logWriter);
	}

	public boolean getRemoveAbandoned() {
		return getDelegate().getRemoveAbandoned();
	}

	public void setRemoveAbandoned(boolean removeAbandoned) {
		getDelegate().setRemoveAbandoned(removeAbandoned);
	}

	public int getRemoveAbandonedTimeout() {
		return getDelegate().getRemoveAbandonedTimeout();
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		getDelegate().setRemoveAbandonedTimeout(removeAbandonedTimeout);
	}

	public boolean getLogAbandoned() {
		return getDelegate().getLogAbandoned();
	}

	public void setLogAbandoned(boolean logAbandoned) {
		getDelegate().setLogAbandoned(logAbandoned);
	}

	public void addConnectionProperty(String name, String value) {
		getDelegate().addConnectionProperty(name, value);
	}

	public void removeConnectionProperty(String name) {
		getDelegate().removeConnectionProperty(name);
	}

	public void setConnectionProperties(String connectionProperties) {
		getDelegate().setConnectionProperties(connectionProperties);
	}

	public void close() throws SQLException {
		getDelegate().close();
	}

	public boolean equals(Object arg0) {
		return getDelegate().equals(arg0);
	}

	public Connection getConnection() throws SQLException {
		return getDelegate().getConnection();
	}

	public int hashCode() {
		return getDelegate().hashCode();
	}

	public boolean isAccessToUnderlyingConnectionAllowed() {
		return getDelegate().isAccessToUnderlyingConnectionAllowed();
	}

	public boolean isClosed() {
		return getDelegate().isClosed();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getDelegate().isWrapperFor(iface);
	}

	public String toString() {
		return getDelegate().toString();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getDelegate().unwrap(iface);
	}
}
