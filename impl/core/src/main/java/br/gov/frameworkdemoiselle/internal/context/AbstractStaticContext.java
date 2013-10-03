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
package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.annotation.StaticScoped;
import br.gov.frameworkdemoiselle.configuration.Configuration;

/**
 * 
 * <p>This context has a unified static store that keeps all scoped beans available
 * to all threads of an application. It is intended to keep beans avaliable to
 * long lasting scopes (like the Session scope and Application scope) on environments
 * that lack those scopes by default (like desktop Swing applications).</p>
 * 
 * <p>This context also keeps beans of the custom {@link StaticScoped} scope, like the beans
 * annotated with {@link Configuration}.</p>
 * 
 * @author serpro
 *
 */
@Priority(Priority.MIN_PRIORITY)
public abstract class AbstractStaticContext extends AbstractCustomContext {

	private final static Map<String, Store> staticStore = Collections.synchronizedMap(new HashMap<String, Store>());
	
	/**
	 * Constructs this context to control the provided scope
	 */
	AbstractStaticContext(Class<? extends Annotation> scope) {
		super(scope);
	}

	@Override
	protected Store getStore() {
		Store store = staticStore.get( this.getClass().getCanonicalName() );
		if (store==null){
			store = createStore();
			staticStore.put(this.getClass().getCanonicalName(), store);
		}
		
		return store;
	}

	@Override
	protected boolean isStoreInitialized() {
		return staticStore!=null;
	}
}
