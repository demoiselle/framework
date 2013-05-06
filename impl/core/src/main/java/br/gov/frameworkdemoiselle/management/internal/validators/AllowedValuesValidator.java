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
package br.gov.frameworkdemoiselle.management.internal.validators;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues;


public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Object> {

	private br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues.ValueType valueType;
	private String[] allowedValues;

	@Override
	public void initialize(AllowedValues constraintAnnotation) {
		valueType = constraintAnnotation.valueType();
		allowedValues = constraintAnnotation.allows();
	}
	
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		
		if (value==null){
			return false;
		}

		switch(valueType){
			case STRING:
				for (String str : allowedValues){
					if (str.equals(value)) return true;
				}
				return false;
				
			case INTEGER:
				try{
					Integer number = Integer.valueOf(value.toString());
					String strNumber = number.toString();
					for (String str : allowedValues){
						if (str.equals(strNumber)) return true;
					}

					return false;
				}
				catch(NumberFormatException ne){
					return false;
				}
				
			case DECIMAL:
				try{
					BigDecimal number = new BigDecimal(value.toString());
					String strNumber = number.toString();
					for (String str : allowedValues){
						if (str.equals(strNumber)) return true;
					}
				}
				catch(NumberFormatException ne){
					return false;
				}
				
				return false;
		}

		return false;
	}

}
