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
package br.gov.frameworkdemoiselle.management;

/**
 * Special notification to denote an attribute has changed values.
 * 
 * @see Notification
 * 
 * @author serpro
 *
 */
public class AttributeChangeNotification extends Notification {
	
	private String attributeName;
	
	private Class<? extends Object> attributeType;
	
	private Object oldValue;
	
	private Object newValue;
	
	public AttributeChangeNotification(){}
	
	public AttributeChangeNotification(Object message, String attributeName, Class<? extends Object> attributeType, Object oldValue,
			Object newValue) {
		super(message);
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}


	public String getAttributeName() {
		return attributeName;
	}

	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	
	public Class<? extends Object> getAttributeType() {
		return attributeType;
	}

	
	public void setAttributeType(Class<? extends Object> attributeType) {
		this.attributeType = attributeType;
	}

	
	public Object getOldValue() {
		return oldValue;
	}

	
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	
	public Object getNewValue() {
		return newValue;
	}

	
	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}
	
	

}
