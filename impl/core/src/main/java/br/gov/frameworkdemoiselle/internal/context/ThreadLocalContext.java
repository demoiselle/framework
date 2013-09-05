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

/**
 * Base context that has a separated store for each thread
 * 
 * @author SERPRO
 */
public class ThreadLocalContext extends AbstractCustomContext {

	private final ThreadLocal<Store> threadLocal = new ThreadLocal<Store>();

	public ThreadLocalContext(final Class<? extends Annotation> scope) {
		super(scope);
	}
	
	@Override
	protected boolean isStoreInitialized() {
		return threadLocal.get()!=null;
	}

	@Override
	protected Store getStore() {
		if (this.threadLocal.get() == null) {
			this.threadLocal.set(createStore());
		}

		return this.threadLocal.get();
	}
}
