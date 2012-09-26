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
package br.gov.frameworkdemoiselle.internal.producer;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;


public class FakeEntityManager implements EntityManager {

	private boolean closed = false;

	private boolean equals = true;

	private int hashCode = 1;

	private String toString = "";

	@Override
	public void clear() {
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public boolean contains(Object arg0) {
		return false;
	}

	@Override
	public Query createNamedQuery(String arg0) {
		return null;
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0, @SuppressWarnings("rawtypes") Class arg1) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0, String arg1) {
		return null;
	}

	@Override
	public Query createQuery(String arg0) {
		return null;
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
		return null;
	}

	@Override
	public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
		return null;
	}

	@Override
	public void detach(Object arg0) {
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1) {
		return null;
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
		return null;
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
		return null;
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2, Map<String, Object> arg3) {
		return null;
	}

	@Override
	public void flush() {
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return null;
	}

	@Override
	public Object getDelegate() {
		return null;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return null;
	}

	@Override
	public FlushModeType getFlushMode() {
		return null;
	}

	@Override
	public LockModeType getLockMode(Object arg0) {
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

	@Override
	public <T> T getReference(Class<T> arg0, Object arg1) {
		return null;
	}

	@Override
	public EntityTransaction getTransaction() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	@Override
	public void joinTransaction() {
	}

	@Override
	public void lock(Object arg0, LockModeType arg1) {
	}

	@Override
	public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
	}

	@Override
	public <T> T merge(T arg0) {
		return null;
	}

	@Override
	public void persist(Object arg0) {
	}

	@Override
	public void refresh(Object arg0) {
	}

	@Override
	public void refresh(Object arg0, Map<String, Object> arg1) {
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1) {
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
	}

	@Override
	public void remove(Object arg0) {
	}

	@Override
	public void setFlushMode(FlushModeType arg0) {
	}

	@Override
	public void setProperty(String arg0, Object arg1) {
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return equals;
	}

	public void setEquals(boolean equals) {
		this.equals = equals;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public void setHashCode(int hash) {
		this.hashCode = hash;
	}

	@Override
	public String toString() {
		return toString;
	}

	public void setToString(String toString) {
		this.toString = toString;
	}

}
