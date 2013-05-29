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

import java.io.Serializable;
import java.util.List;

/**
 * Interface containing basic methods for creating, updating and deleting entities (the CRUD design pattern).
 * 
 * @author serpro
 *
 * @param <T> Type of the entity
 * @param <I> Type of the identification attribute of the entity
 */
public interface Crud<T, I> extends Serializable {

	/**
	 * Find an entity by it's identification attribute and make it transient.
	 * 
	 * @param id The unique identification to find the entity to be deleted.
	 */
	void delete(I id);

	/**
	 * List all instances of the given entity.
	 * 
	 * @return The (possibly empty) list of all instances of the given entity.
	 */
	List<T> findAll();

	/**
	 * Insert an entity and return the inserted instance of the entity
	 * 
	 * @param bean The bean to be inserted
	 * @return The inserted instance of the entity
	 */
	T insert(T bean);

	/**
	 * Find an entity's instance by it's identification attribute and return it
	 * @param id Value of the identification attribute of the desired entity's instance
	 * @return The entity's instance whose identification attribute's value matches
	 * the one passed as argument.
	 */
	T load(I id);

	/**
	 * Update an instance of the entity with the bean's attributes as new values. 
	 * 
	 * @param bean The bean instance containing both the identification value to find the old
	 * instance and the new values for the instance's attributes.
	 * @return The updated entity's instance
	 */
	T update(T bean);

}
