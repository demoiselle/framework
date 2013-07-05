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

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.message.DefaultMessage;
import br.gov.frameworkdemoiselle.message.Message;
import br.gov.frameworkdemoiselle.message.MessageAppender;
import br.gov.frameworkdemoiselle.message.MessageContext;
import br.gov.frameworkdemoiselle.message.SeverityType;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * The message store is designed to provide access to messages. It is shared by every application layer.
 * 
 * @see MessageContext
 */
public class MessageContextImpl implements Serializable, MessageContext {

	private static final long serialVersionUID = 1L;

	@Deprecated
	private transient final List<Message> messages = new ArrayList<Message>();

	private transient static ResourceBundle bundle;

	private transient static Logger logger;

	@Override
	public void add(final Message message, Object... params) {
		Message aux;

		if (params != null) {
			aux = new DefaultMessage(message.getText(), message.getSeverity(), params);
		} else {
			aux = message;
		}

		getLogger().debug(getBundle().getString("adding-message-to-context", message.toString()));
		getAppender().append(aux);
	}

	private MessageAppender getAppender() {
		Class<? extends MessageAppender> appenderClass = StrategySelector.selectClass(MessageAppender.class);

		return Beans.getReference(appenderClass);
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
	@Deprecated
	public List<Message> getMessages() {
		return messages;
	}

	@Override
	@Deprecated
	public void clear() {
		getLogger().debug(getBundle().getString("cleaning-message-context"));
		messages.clear();
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(MessageContext.class);
		}

		return logger;
	}
}
