package br.gov.frameworkdemoiselle.internal.proxy;

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

	public Object getSingleResult() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.getSingleResult();
	}

	public int executeUpdate() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.executeUpdate();
	}

	public Query setMaxResults(int maxResult) {
		queryDelegate.setMaxResults(maxResult);
		return this;
	}

	public int getMaxResults() {
		return queryDelegate.getMaxResults();
	}

	public Query setFirstResult(int startPosition) {
		queryDelegate.setFirstResult(startPosition);
		return this;
	}

	public int getFirstResult() {
		return queryDelegate.getFirstResult();
	}

	public Query setHint(String hintName, Object value) {
		queryDelegate.setHint(hintName, value);
		return this;
	}

	public Map<String, Object> getHints() {
		return queryDelegate.getHints();
	}

	public <T> Query setParameter(Parameter<T> param, T value) {
		queryDelegate.setParameter(param, value);
		return this;
	}

	public Query setParameter(Parameter<Calendar> param, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	public Query setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	public Query setParameter(String name, Object value) {
		queryDelegate.setParameter(name, value);
		return this;
	}

	public Query setParameter(String name, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	public Query setParameter(String name, Date value, TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	public Query setParameter(int position, Object value) {
		queryDelegate.setParameter(position, value);
		return this;
	}

	public Query setParameter(int position, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	public Query setParameter(int position, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	public Set<Parameter<?>> getParameters() {
		return queryDelegate.getParameters();
	}

	public Parameter<?> getParameter(String name) {
		return queryDelegate.getParameter(name);
	}

	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return queryDelegate.getParameter(name, type);
	}

	public Parameter<?> getParameter(int position) {
		return queryDelegate.getParameter(position);
	}

	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return queryDelegate.getParameter(position, type);
	}

	public boolean isBound(Parameter<?> param) {
		return queryDelegate.isBound(param);
	}

	public <T> T getParameterValue(Parameter<T> param) {
		return queryDelegate.getParameterValue(param);
	}

	public Object getParameterValue(String name) {
		return queryDelegate.getParameterValue(name);
	}

	public Object getParameterValue(int position) {
		return queryDelegate.getParameterValue(position);
	}

	public Query setFlushMode(FlushModeType flushMode) {
		queryDelegate.setFlushMode(flushMode);
		return this;
	}

	public FlushModeType getFlushMode() {
		return queryDelegate.getFlushMode();
	}

	public Query setLockMode(LockModeType lockMode) {
		entityManagerCaller.joinTransactionIfNecessary();
		queryDelegate.setLockMode(lockMode);
		return this;
	}

	public LockModeType getLockMode() {
		return queryDelegate.getLockMode();
	}

	public <T> T unwrap(Class<T> cls) {
		return queryDelegate.unwrap(cls);
	}

	

}
