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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

public class TypedQueryProxy<X> implements TypedQuery<X> {
	
	private TypedQuery<X> queryDelegate;
	private EntityManagerProxy entityManagerCaller;
	
	public TypedQueryProxy(TypedQuery<X> queryDelegate,
			EntityManagerProxy entityManagerCaller) {
		this.queryDelegate = queryDelegate;
		this.entityManagerCaller = entityManagerCaller;
	}

	public List<X> getResultList() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.getResultList();
	}

	public X getSingleResult() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.getSingleResult();
	}

	public int executeUpdate() {
		entityManagerCaller.joinTransactionIfNecessary();
		return queryDelegate.executeUpdate();
	}

	public TypedQuery<X> setMaxResults(int maxResult) {
		queryDelegate.setMaxResults(maxResult);
		return this;
	}

	public TypedQuery<X> setFirstResult(int startPosition) {
		queryDelegate.setFirstResult(startPosition);
		return this;
	}

	public TypedQuery<X> setHint(String hintName, Object value) {
		queryDelegate.setHint(hintName, value);
		return this;
	}

	public int getMaxResults() {
		return queryDelegate.getMaxResults();
	}

	public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
		queryDelegate.setParameter(param, value);
		return this;
	}

	public int getFirstResult() {
		return queryDelegate.getFirstResult();
	}

	public TypedQuery<X> setParameter(Parameter<Calendar> param,
			Calendar value, TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	public TypedQuery<X> setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(param, value, temporalType);
		return this;
	}

	public Map<String, Object> getHints() {
		return queryDelegate.getHints();
	}

	public TypedQuery<X> setParameter(String name, Object value) {
		queryDelegate.setParameter(name, value);
		return this;
	}

	public TypedQuery<X> setParameter(String name, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	public TypedQuery<X> setParameter(String name, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(name, value, temporalType);
		return this;
	}

	public TypedQuery<X> setParameter(int position, Object value) {
		queryDelegate.setParameter(position, value);
		return this;
	}

	public TypedQuery<X> setParameter(int position, Calendar value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	public TypedQuery<X> setParameter(int position, Date value,
			TemporalType temporalType) {
		queryDelegate.setParameter(position, value, temporalType);
		return this;
	}

	public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
		queryDelegate.setFlushMode(flushMode);
		return this;
	}

	public TypedQuery<X> setLockMode(LockModeType lockMode) {
		queryDelegate.setLockMode(lockMode);
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

	public FlushModeType getFlushMode() {
		return queryDelegate.getFlushMode();
	}

	public LockModeType getLockMode() {
		return queryDelegate.getLockMode();
	}

	public <T> T unwrap(Class<T> cls) {
		return queryDelegate.unwrap(cls);
	}
	
	

}
