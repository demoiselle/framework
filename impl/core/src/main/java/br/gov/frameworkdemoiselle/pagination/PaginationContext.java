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
package br.gov.frameworkdemoiselle.pagination;

/**
 * Context interface reserved for pagination purposes.
 * <p>
 * In order to use this, just add the line below in the code:
 * <p>
 * <code>@Inject PaginationContext paginationContext;</code>
 * 
 * @author SERPRO
 * @see Pagination
 */
public interface PaginationContext {

	/**
	 * Retrieves the pagination according to the class type specified.
	 * 
	 * @param clazz
	 *            a {@code Class} type
	 * @return Pagination
	 */
	Pagination getPagination(Class<?> clazz);

	/**
	 * Retrieves the pagination according to the class type specified. If not existing, creates the pagination whenever
	 * {@code create} parameter is true.
	 * 
	 * @param clazz
	 *            a {@code Class} type
	 * @param create
	 *            determines whether pagination must always be returned
	 * @return Pagination
	 */
	Pagination getPagination(Class<?> clazz, boolean create);

}
