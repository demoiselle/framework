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
package template.crud;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import template.model.DummyEntity;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.template.Crud;

@RequestScoped
public class CrudImpl implements Crud<DummyEntity, Long> {

	private List<DummyEntity> entities;
	
	public CrudImpl() {
		
		super();
		
		this.entities = new ArrayList<DummyEntity>();
		
	}

	public void resetEntities() {
		
		this.entities.clear();
		
		this.entities.add(new DummyEntity(1L, "Dummy1 Label 1"));
		this.entities.add(new DummyEntity(2L, "Dummy1 Label 2"));
		this.entities.add(new DummyEntity(3L, "Dummy1 Label 3"));
		this.entities.add(new DummyEntity(4L, "Dummy1 Label 4"));
		this.entities.add(new DummyEntity(5L, "Dummy1 Label 5"));
		this.entities.add(new DummyEntity(6L, "Dummy1 Label 6"));
		this.entities.add(new DummyEntity(7L, "Dummy1 Label 7"));
		this.entities.add(new DummyEntity(8L, "Dummy1 Label 8"));
		this.entities.add(new DummyEntity(9L, "Dummy1 Label 9"));
		this.entities.add(new DummyEntity(10L, "Dummy1 Label 10"));
		
	}

	@Override
	public void delete(Long id) {
				
		this.entities.remove(this.load(id));
		
	}

	@Override
	public List<DummyEntity> findAll() {

		return this.entities;
		
	}

	@Override
	public DummyEntity insert(DummyEntity bean) {

		if (this.entities.add(bean)) {
			return bean;
		} else {
			throw new DemoiselleException("Erro ao inserir entity");
		}
		
	}

	@Override
	public DummyEntity load(Long id) {

		for (DummyEntity dummyEntity : this.entities) {
			
			if (dummyEntity.getId().equals(id)) {

				return dummyEntity;
				
			}
			
		}
		
		return null;
		
	}

	@Override
	public DummyEntity update(DummyEntity bean) {

		DummyEntity dummyEntity = this.load(bean.getId());
		
		dummyEntity.setName(bean.getName());
		
		return dummyEntity;
		
	}

}
