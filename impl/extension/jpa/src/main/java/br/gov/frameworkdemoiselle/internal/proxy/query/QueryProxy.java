package br.gov.frameworkdemoiselle.internal.proxy.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;

public class QueryProxy implements Query {
	
	private Query queryDelegate;
	private EntityManagerProxy entityManagerCaller;
	
	public QueryProxy(Query queryDelegate,EntityManagerProxy entityManagerCaller){
		this.queryDelegate = queryDelegate;
		this.entityManagerCaller = entityManagerCaller;
	}

	@SuppressWarnings("rawtypes")
	public List getResultList() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.getResultList();
	}

	/**
	 * @see Query.getSingleResult()
	 */
	@Override
	public Object getSingleResult() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.getSingleResult();
	}

	@Override
	public int executeUpdate() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.executeUpdate();
	}

	@Override
	public Query setMaxResults(int maxResult) {
		queryDelegate.setMaxResults(maxResult);
		return this;
	}

	@Override
	public int getMaxResults() {
		return queryDelegate.getMaxResults();
	}

	@Override
	public Query setFirstResult(int startPosition) {
		queryDelegate.setFirstResult(startPosition);
		return this;
	}

	@Override
	public int getFirstResult() {
		return queryDelegate.getFirstResult();
	}

	@Override
	public Query setHint(String hintName, Object value) {
		queryDelegate.setHint(hintName, value);
		return this;
	}

	@Override
	public Map<String, Object> getHints() {
		return queryDelegate.getHints();
	}

	@Override
	public <T> Query setParameter(Parameter<T> param, T value) {
		queryDelegate.setParameter(param, value);
		return this;
	}

	@Override
	public Query setParameter(Parameter<Calendar> param, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	public Query setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	public Query setParameter(String name, Object value) {
		queryDelegate.setParameter(name, value);
		return this;
	}

	@Override
	public Query setParameter(String name, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	public Query setParameter(String name, Date value, TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	public Query setParameter(int position, Object value) {
		queryDelegate.setParameter(position, value);
		return this;
	}

	@Override
	public Query setParameter(int position, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	public Query setParameter(int position, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return queryDelegate.getParameters();
	}

	@Override
	public Parameter<?> getParameter(String name) {
		return queryDelegate.getParameter(name);
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return queryDelegate.getParameter(name, type);
	}

	@Override
	public Parameter<?> getParameter(int position) {
		return queryDelegate.getParameter(position);
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return queryDelegate.getParameter(position, type);
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		return queryDelegate.isBound(param);
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		return queryDelegate.getParameterValue(param);
	}

	@Override
	public Object getParameterValue(String name) {
		return queryDelegate.getParameterValue(name);
	}

	@Override
	public Object getParameterValue(int position) {
		return queryDelegate.getParameterValue(position);
	}

	@Override
	public Query setFlushMode(FlushModeType flushMode) {
		queryDelegate.setFlushMode(flushMode);
		return this;
	}

	@Override
	public FlushModeType getFlushMode() {
		return queryDelegate.getFlushMode();
	}

	@Override
	public Query setLockMode(LockModeType lockMode) {
		entityManagerCaller.joinTransactionIfNecessary();
		queryDelegate.setLockMode(lockMode);
		return this;
	}

	@Override
	public LockModeType getLockMode() {
		return queryDelegate.getLockMode();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return queryDelegate.unwrap(cls);
	}

}
