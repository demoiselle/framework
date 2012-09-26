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
package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import br.gov.frameworkdemoiselle.internal.configuration.PaginationConfig;
import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Context implementation reserved for pagination purposes. Internally a hash map is used to store pagination data for
 * each class type.
 * 
 * @author SERPRO
 * @see PaginationContext
 */
@SessionScoped
public class PaginationContextImpl implements Serializable, PaginationContext {

	private static final long serialVersionUID = 1L;

	private PaginationConfig config;

	private final Map<Class<?>, Pagination> cache = new HashMap<Class<?>, Pagination>();

	public Pagination getPagination(final Class<?> clazz) {
		return this.getPagination(clazz, false);
	}

	public Pagination getPagination(final Class<?> clazz, final boolean create) {
		Pagination pagination = cache.get(clazz);

		if (pagination == null && create) {
			pagination = new PaginationImpl();
			pagination.setPageSize(getConfig().getPageSize());

			cache.put(clazz, pagination);
		}

		return pagination;
	}

	private PaginationConfig getConfig() {
		if (config == null) {
			config = Beans.getReference(PaginationConfig.class);
		}

		return config;
	}
}
