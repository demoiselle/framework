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
package br.gov.frameworkdemoiselle.internal.procuder;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.util.Beans.ProgramaticInjectionPoint;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class FacesContextProducer {

	@Produces
	@Dependent
	public FacesContext create(InjectionPoint ip, @Name("demoiselle-jsf-bundle") ResourceBundle bundle, Logger logger) {
		if (ip != null && !ip.isTransient()) {
			final Member member = ip.getMember();
			if (member != null) {
				Class<?> declaringClass = member.getDeclaringClass();
				if (Serializable.class.isAssignableFrom(declaringClass)) {
					throw new DemoiselleException(bundle.getString("faces-context-not-passivable", member.getName(),
							declaringClass.getCanonicalName()), new NotSerializableException(
							FacesContext.class.getCanonicalName()));
				}
			} else if (ProgramaticInjectionPoint.class.isInstance(ip)) {
				logger.fine(bundle.getString("faces-context-passivation-warning"));
			}
		}

		FacesContext context = FacesContext.getCurrentInstance();
		if (context == null) {
			throw new ContextNotActiveException(bundle.getString("faces-context-not-available"));
		}

		return context;
	}
}
