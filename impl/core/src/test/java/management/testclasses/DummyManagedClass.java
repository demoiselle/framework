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
package management.testclasses;

import java.util.UUID;

import br.gov.frameworkdemoiselle.management.annotation.Managed;
import br.gov.frameworkdemoiselle.management.annotation.Operation;
import br.gov.frameworkdemoiselle.management.annotation.Property;
import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues;
import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues.ValueType;

@Managed
public class DummyManagedClass {
	
	@Property
	private String name;
	
	@Property
	@AllowedValues(allows={"f","m","F","M"},valueType=ValueType.INTEGER)
	private Integer id;
	
	@Property
	private Integer firstFactor , secondFactor;
	
	@Property
	private String uuid;
	
	@Property
	private String writeOnlyProperty;
	
	@Property
	private String readOnlyProperty = "Default Value";
	
	/**
	 * Propriedade para testar detecção de métodos GET e SET quando propriedade tem apenas uma letra.
	 */
	@Property
	private Integer a;
	
	/**
	 * Propriedade para testar detecção de métodos GET e SET quando propriedade tem apenas letras maiúsculas.
	 */
	@Property
	private Integer MAIUSCULO;

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setWriteOnlyProperty(String newValue){
		this.writeOnlyProperty = newValue;
	}
	
	public Integer getA() {
		return a;
	}

	public void setA(Integer a) {
		this.a = a;
	}
	
	public Integer getMAIUSCULO() {
		return MAIUSCULO;
	}

	
	public void setMAIUSCULO(Integer mAIUSCULO) {
		MAIUSCULO = mAIUSCULO;
	}

	@Operation(description="Generates a random UUID")
	public String generateUUID(){
		this.uuid = UUID.randomUUID().toString();
		return this.uuid;
	}

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	
	public String getReadOnlyProperty() {
		return readOnlyProperty;
	}

	
	public Integer getFirstFactor() {
		return firstFactor;
	}

	
	public void setFirstFactor(Integer firstFactor) {
		this.firstFactor = firstFactor;
	}

	
	public Integer getSecondFactor() {
		return secondFactor;
	}

	
	public void setSecondFactor(Integer secondFactor) {
		this.secondFactor = secondFactor;
	}
	
	@Operation
	public Integer calculateFactorsNonSynchronized(Integer firstFactor , Integer secondFactor){
		setFirstFactor(firstFactor);
		setSecondFactor(secondFactor);
		
		try {
			int temp = firstFactor + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + firstFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			return temp;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Operation
	public synchronized Integer calculateFactorsSynchronized(Integer firstFactor , Integer secondFactor){
		setFirstFactor(firstFactor);
		setSecondFactor(secondFactor);
		
		try {
			int temp = firstFactor + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + firstFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			return temp;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void nonOperationAnnotatedMethod(){
		System.out.println("Test");
	}
	
	
}
