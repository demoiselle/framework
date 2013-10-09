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
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig.EntityManagerScope;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Delegates all operation invocations to the cached EntityManager.
 * 
 * @author CETEC
 */
public class EntityManagerProxy implements EntityManager, Serializable {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Persistence unit of the delegated EntityManager.
	 */
	private String persistenceUnit;
	
	/*
	 * demoiselle-jpa configuration options
	 */
	private EntityManagerConfig configuration;
	
	
	private EntityManager delegateCache;	

	/**
	 * Constructor based on persistence unit name.
	 * 
	 * @param persistenceUnit
	 */
	public EntityManagerProxy(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	/**
	 * Retrieves a EntityManager from the EntityManagerProducer cache. All operations of this proxy are delegated to the
	 * cached EntityManager.
	 * 
	 * @return Cached EntityManager
	 */
	private EntityManager getEntityManagerDelegate() {
		//Se o produtor de EntityManager não estiver em um escopo, precisamos guardar em cache o EntityManager produzido,
		//do contrário, basta solicitar uma instância do produtor (que estará em um escopo) e obter a instância real 
		//de EntityManager dele.
		if (getConfiguration().getEntityManagerScope()!=EntityManagerScope.NOSCOPE || delegateCache==null){
			EntityManagerProducer emp = Beans.getReference(EntityManagerProducer.class);
			delegateCache = emp.getEntityManager(this.persistenceUnit);
		}
		
		return delegateCache;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#persist(java.lang.Object)
	 */
	@Override
	public void persist(Object entity) {
		joinTransactionIfNecessary();
		getEntityManagerDelegate().persist(entity);
		checkEntityManagerScopePassivable(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#merge(java.lang.Object)
	 */
	@Override
	public <T> T merge(T entity) {
		joinTransactionIfNecessary();
		T managedEntity = getEntityManagerDelegate().merge(entity);
		checkEntityManagerScopePassivable(managedEntity);
		return getEntityManagerDelegate().merge(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#remove(java.lang.Object)
	 */
	@Override
	public void remove(Object entity) {
		joinTransactionIfNecessary();
		checkEntityManagerScopePassivable(entity);
		getEntityManagerDelegate().remove(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		joinTransactionIfNecessary();
		return getEntityManagerDelegate().find(entityClass, primaryKey);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
	 */
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		joinTransactionIfNecessary();
		return getEntityManagerDelegate().find(entityClass, primaryKey, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType)
	 */
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		joinTransactionIfNecessary();
		checkEntityManagerScopePassivable(lockMode);
		return getEntityManagerDelegate().find(entityClass, primaryKey, lockMode);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType,
	 * java.util.Map)
	 */
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		joinTransactionIfNecessary();
		checkEntityManagerScopePassivable(lockMode);
		return getEntityManagerDelegate().find(entityClass, primaryKey, lockMode, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getReference(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		joinTransactionIfNecessary();
		return getEntityManagerDelegate().getReference(entityClass, primaryKey);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#flush()
	 */
	@Override
	public void flush() {
		getEntityManagerDelegate().flush();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#setFlushMode(javax.persistence.FlushModeType)
	 */
	@Override
	public void setFlushMode(FlushModeType flushMode) {
		getEntityManagerDelegate().setFlushMode(flushMode);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getFlushMode()
	 */
	@Override
	public FlushModeType getFlushMode() {
		return getEntityManagerDelegate().getFlushMode();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType)
	 */
	@Override
	public void lock(Object entity, LockModeType lockMode) {
		joinTransactionIfNecessary();
		checkEntityManagerScopePassivable(lockMode);
		getEntityManagerDelegate().lock(entity, lockMode);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
	 */
	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		joinTransactionIfNecessary();
		checkEntityManagerScopePassivable(lockMode);
		getEntityManagerDelegate().lock(entity, lockMode, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(Object entity) {
		joinTransactionIfNecessary();
		getEntityManagerDelegate().refresh(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
	 */
	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		joinTransactionIfNecessary();
		getEntityManagerDelegate().refresh(entity, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType)
	 */
	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		joinTransactionIfNecessary();
		getEntityManagerDelegate().refresh(entity, lockMode);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
	 */
	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		joinTransactionIfNecessary();
		getEntityManagerDelegate().refresh(entity, lockMode, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#clear()
	 */
	@Override
	public void clear() {
		getEntityManagerDelegate().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#detach(java.lang.Object)
	 */
	@Override
	public void detach(Object entity) {
		getEntityManagerDelegate().detach(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object entity) {
		return getEntityManagerDelegate().contains(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getLockMode(java.lang.Object)
	 */
	@Override
	public LockModeType getLockMode(Object entity) {
		joinTransactionIfNecessary();
		return getEntityManagerDelegate().getLockMode(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String propertyName, Object value) {
		getEntityManagerDelegate().setProperty(propertyName, value);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		return getEntityManagerDelegate().getProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createQuery(java.lang.String)
	 */
	@Override
	public Query createQuery(String qlString) {
		return new QueryProxy(getEntityManagerDelegate().createQuery(qlString) , this) ;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createQuery(javax.persistence.criteria.CriteriaQuery)
	 */
	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return new TypedQueryProxy<T>( getEntityManagerDelegate().createQuery(criteriaQuery) , this );
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createQuery(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return new TypedQueryProxy<T>(getEntityManagerDelegate().createQuery(qlString, resultClass),this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createNamedQuery(java.lang.String)
	 */
	@Override
	public Query createNamedQuery(String name) {
		return new QueryProxy(getEntityManagerDelegate().createNamedQuery(name), this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createNamedQuery(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return new TypedQueryProxy<T>(getEntityManagerDelegate().createNamedQuery(name, resultClass),this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String)
	 */
	@Override
	public Query createNativeQuery(String sqlString) {
		return new QueryProxy(getEntityManagerDelegate().createNativeQuery(sqlString), this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.Class)
	 */
	@Override
	public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		return new QueryProxy(getEntityManagerDelegate().createNativeQuery(sqlString, resultClass), this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.String)
	 */
	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return new QueryProxy(getEntityManagerDelegate().createNativeQuery(sqlString, resultSetMapping),this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#joinTransaction()
	 */
	@Override
	public void joinTransaction() {
		getEntityManagerDelegate().joinTransaction();
	}

	/**
	 * Attemp to join transaction, if the active transaction is not managed by current EntityManager.
	 */
	protected final void joinTransactionIfNecessary() {
		try {
			/*EntityTransaction transaction = */getEntityManagerDelegate().getTransaction();
		} catch (IllegalStateException cause) {
			//IllegalStateException is launched if we are on a JTA entity manager, so
			//we assume we need to join transaction instead of creating one.
			
			try{
				getEntityManagerDelegate().joinTransaction();
			}
			catch(TransactionRequiredException te){
				//It get's launched if there is no JTA transaction opened. It usually means we are
				//being launched inside a method not marked with @Transactional so we ignore the exception.
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> cls) {
		return getEntityManagerDelegate().unwrap(cls);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getDelegate()
	 */
	@Override
	public Object getDelegate() {
		return getEntityManagerDelegate().getDelegate();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#close()
	 */
	@Override
	public void close() {
		getEntityManagerDelegate().close();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return getEntityManagerDelegate().isOpen();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getTransaction()
	 */
	@Override
	public EntityTransaction getTransaction() {
		return getEntityManagerDelegate().getTransaction();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return getEntityManagerDelegate().getEntityManagerFactory();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getCriteriaBuilder()
	 */
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return getEntityManagerDelegate().getCriteriaBuilder();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.persistence.EntityManager#getMetamodel()
	 */
	@Override
	public Metamodel getMetamodel() {
		return getEntityManagerDelegate().getMetamodel();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		return getEntityManagerDelegate().equals(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getEntityManagerDelegate().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getEntityManagerDelegate().toString();
	}
	
	private void checkEntityManagerScopePassivable(Object entity)  {
		EntityManagerConfig configuration = getConfiguration();
		if (configuration.getEntityManagerScope()==EntityManagerScope.CONVERSATION
				|| configuration.getEntityManagerScope()==EntityManagerScope.SESSION
				|| configuration.getEntityManagerScope()==EntityManagerScope.VIEW){
		
			LockModeType lockMode = null;
			if (getEntityManagerDelegate().contains(entity)){
				lockMode = getEntityManagerDelegate().getLockMode(entity);
			}
			checkEntityManagerScopePassivable(lockMode);
		}
	}

	private void checkEntityManagerScopePassivable(LockModeType lockMode)  {
		EntityManagerConfig configuration = getConfiguration();
		if (configuration.getEntityManagerScope()==EntityManagerScope.CONVERSATION
				|| configuration.getEntityManagerScope()==EntityManagerScope.SESSION
				|| configuration.getEntityManagerScope()==EntityManagerScope.VIEW){
			
			if (lockMode!=null 
					&& lockMode!=LockModeType.NONE 
					&& lockMode!=LockModeType.OPTIMISTIC_FORCE_INCREMENT){
				String message = getBundle().getString("passivable-scope-without-optimistic-lock" , configuration.getEntityManagerScope().toString());
				getLogger().error(message);
				throw new DemoiselleException(message);
			}
		}
	}
	
	private EntityManagerConfig getConfiguration(){
		if (configuration==null){
			configuration = Beans.getReference(EntityManagerConfig.class);
		}
		
		return configuration;
	}
	
	private Logger getLogger() {
		return Beans.getReference(Logger.class);
	}
	
	private ResourceBundle getBundle(){
		return Beans.getReference(ResourceBundle.class,new NameQualifier("demoiselle-jpa-bundle"));
	}
	
}
