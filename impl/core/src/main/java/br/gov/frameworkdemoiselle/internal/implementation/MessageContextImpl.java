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
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.message.DefaultMessage;
import br.gov.frameworkdemoiselle.message.Message;
import br.gov.frameworkdemoiselle.message.MessageContext;
import br.gov.frameworkdemoiselle.message.SeverityType;

/**
 * The message store is designed to provide access to messages. It is shared by every application layer.
 * 
 * @see MessageContext
 */
@RequestScoped
public class MessageContextImpl implements Serializable, MessageContext {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	private final List<Message> messages = new ArrayList<Message>();

	@Override
	public void add(final Message message, Object... params) {
		Message aux;

		if (params != null) {
			aux = new DefaultMessage(message.getText(), message.getSeverity(), params);
		} else {
			aux = message;
		}

		logger.debug(CoreBundle.get().getString("adding-message-to-context", message.toString()));
		messages.add(aux);
	}

	@Override
	public void add(String text, Object... params) {
		add(text, null, params);
	}

	@Override
	public void add(String text, SeverityType severity, Object... params) {
		add(new DefaultMessage(text, severity, params));
	}

	@Override
	public List<Message> getMessages() {
		return messages;
	}

	@Override
	public void clear() {
		logger.debug(CoreBundle.get().getString("cleaning-message-context"));
		messages.clear();
	}
}
