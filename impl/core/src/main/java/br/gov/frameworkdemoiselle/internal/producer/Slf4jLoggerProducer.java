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
package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.proxy.Slf4jLoggerProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Reflections;

@Deprecated
public class Slf4jLoggerProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Default
	@Produces
	@Deprecated
	public Logger create(final InjectionPoint ip) {
		Class<?> type;

		if (ip != null && ip.getMember() != null) {
			type = ip.getMember().getDeclaringClass();
		} else {
			type = Slf4jLoggerProducer.class;
		}

		return create(type);
	}

	@Name("")
	@Produces
	@Deprecated
	public Logger createNamed(final InjectionPoint ip) throws ClassNotFoundException {
		Class<?> type;

		try {
			String canonicalName = ip.getAnnotated().getAnnotation(Name.class).value();
			type = Reflections.forName(canonicalName);

		} catch (ClassCastException cause) {
			// TODO Colocar a mensgaem apropriada mostrando como utilizar a anotação @AmbiguousQualifier corretamente
			// com a injeção de
			// Logger.
			throw new DemoiselleException(null, cause);
		}

		return create(type);
	}

	@Deprecated
	public static <T> Logger create(Class<T> type) {
		java.util.logging.Logger logger = Beans.getReference(java.util.logging.Logger.class);
		logger.warning("Para manter a compatibilidade com as futuras versões do Framework Demoiselle, utilize a injeção de "
				+ java.util.logging.Logger.class.getName() + " ao invés de " + Logger.class.getName() + ".");

		return new Slf4jLoggerProxy(type);
	}
}
