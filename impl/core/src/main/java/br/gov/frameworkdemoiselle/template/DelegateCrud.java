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
package br.gov.frameworkdemoiselle.template;

import java.util.List;
import java.util.ListIterator;

import br.gov.frameworkdemoiselle.internal.implementation.DefaultTransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Reflections;

/**
 * An implementation of the {@link Crud} interface that delegates it's operations
 * to another {@link Crud} implementation.
 * 
 * @author serpro
 *
 * @param <T> 
 *            bean object type
 * @param <I>
 *            bean id type
 * @param <C>
 *            type of {@link Crud} implementation this class will delegate to
 */
public class DelegateCrud<T, I, C extends Crud<T, I>> implements Crud<T, I> {

	private static final long serialVersionUID = 1L;

	private Class<C> delegateClass;

	private transient C delegate;

	/**
	 * Removes a instance from delegate.
	 * 
	 * @param id
	 *            Entity with the given identifier
	 */
	@Override
	public void delete(final I id) {
		if (isRunningTransactionalOperations()) {
			transactionalDelete(id);
		} else {
			nonTransactionalDelete(id);
		}
	}

	@Transactional
	private void transactionalDelete(final I id) {
		nonTransactionalDelete(id);
	}

	private void nonTransactionalDelete(final I id) {
		getDelegate().delete(id);
	}

	/**
	 * Removes a list of instances from delegate.
	 * 
	 * @param ids
	 *            List of entities identifiers
	 */
	public void delete(final List<I> ids) {
		if (isRunningTransactionalOperations()) {
			transactionalDelete(ids);
		} else {
			nonTransactionalDelete(ids);
		}
	}

	@Transactional
	private void transactionalDelete(final List<I> ids) {
		nonTransactionalDelete(ids);
	}

	private void nonTransactionalDelete(final List<I> ids) {
		ListIterator<I> iter = ids.listIterator();
		while (iter.hasNext()) {
			this.delete(iter.next());
		}
	}

	/**
	 * Gets the results from delegate.
	 * 
	 * @return The list of matched query results.
	 */
	@Override
	public List<T> findAll() {
		return getDelegate().findAll();
	}

	protected C getDelegate() {
		if (this.delegate == null) {
			this.delegate = Beans.getReference(getDelegateClass());
		}

		return this.delegate;
	}

	protected Class<C> getDelegateClass() {
		if (this.delegateClass == null) {
			this.delegateClass = Reflections.getGenericTypeArgument(this.getClass(), 2);
		}

		return this.delegateClass;
	}

	/**
	 * Delegates the insert operation of the given instance.
	 * 
	 * @param bean
	 *            A entity to be inserted by the delegate
	 */
	@Override
	public T insert(final T bean) {
		if (isRunningTransactionalOperations()) {
			return transactionalInsert(bean);
		} else {
			return nonTransactionalInsert(bean);
		}
	}

	@Transactional
	private T transactionalInsert(final T bean) {
		return nonTransactionalInsert(bean);
	}

	private T nonTransactionalInsert(final T bean) {
		return getDelegate().insert(bean);
	}

	/**
	 * Returns the instance of the given entity with the given identifier
	 * 
	 * @return The instance
	 */
	@Override
	public T load(final I id) {
		return getDelegate().load(id);
	}

	/**
	 * Delegates the update operation of the given instance.
	 * 
	 * @param bean
	 *            The instance containing the updated state.
	 */
	@Override
	public T update(final T bean) {
		if (isRunningTransactionalOperations()) {
			return transactionalUpdate(bean);
		} else {
			return nonTransactionalUpdate(bean);
		}
	}

	@Transactional
	private T transactionalUpdate(final T bean) {
		return nonTransactionalUpdate(bean);
	}

	private T nonTransactionalUpdate(final T bean) {
		return getDelegate().update(bean);
	}

	private boolean isRunningTransactionalOperations() {
		Transaction transaction = Beans.getReference(TransactionContext.class).getCurrentTransaction();
		return !(transaction instanceof DefaultTransaction);
	}
}
