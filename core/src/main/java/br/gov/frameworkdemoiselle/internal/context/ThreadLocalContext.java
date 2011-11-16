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
/*
 * Demoiselle Framework Copyright (c) 2010 Serpro and other contributors as indicated by the @author tag. See the
 * copyright.txt in the distribution for a full listing of contributors. Demoiselle Framework is an open source Java EE
 * library designed to accelerate the development of transactional database Web applications. Demoiselle Framework is
 * released under the terms of the LGPL license 3 http://www.gnu.org/licenses/lgpl.html LGPL License 3 This file is part
 * of Demoiselle Framework. Demoiselle Framework is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License 3 as published by the Free Software Foundation. Demoiselle Framework
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You
 * should have received a copy of the GNU Lesser General Public License along with Demoiselle Framework. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class ThreadLocalContext implements CustomContext {

	private final ThreadLocal<ContextStore> threadLocal = new ThreadLocal<ContextStore>();

	private boolean active;

	private final Class<? extends Annotation> scope;

	public ThreadLocalContext(final Class<? extends Annotation> scope) {
		this(scope, true);
	}

	public ThreadLocalContext(final Class<? extends Annotation> scope,
			boolean active) {
		this.scope = scope;
		this.active = active;
	}

	@Override
	public <T> T get(final Contextual<T> contextual) {
		return get(contextual, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(final Contextual<T> contextual,
			final CreationalContext<T> creationalContext) {
		T instance = null;

		if (!isActive()) {
			throw new ContextNotActiveException();
		}

		String id = getId(contextual);
		if (getStore().contains(id)) {
			instance = (T) getStore().get(id);

		} else if (creationalContext != null) {
			instance = contextual.create(creationalContext);
			getStore().put(id, instance);
		}

		return instance;
	}

	private <T> String getId(final Contextual<T> contextual) {
		Bean<T> bean = (Bean<T>) contextual;
		return bean.getBeanClass().getCanonicalName();
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return this.scope;
	}

	private ContextStore getStore() {
		if (this.threadLocal.get() == null) {
			this.threadLocal.set(new ContextStore());
		}

		return this.threadLocal.get();
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

}
