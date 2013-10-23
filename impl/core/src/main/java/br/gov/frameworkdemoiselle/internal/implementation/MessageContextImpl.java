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
import java.util.List;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.message.DefaultMessage;
import br.gov.frameworkdemoiselle.message.Message;
import br.gov.frameworkdemoiselle.message.MessageAppender;
import br.gov.frameworkdemoiselle.message.MessageContext;
import br.gov.frameworkdemoiselle.message.SeverityType;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * The message store is designed to provide access to messages. It is shared by every application layer.
 * 
 * @see MessageContext
 */
public class MessageContextImpl implements Serializable, MessageContext {

	private static final long serialVersionUID = 1L;

	private transient  ResourceBundle bundle;

	private transient  Logger logger;

	@Override
	@Deprecated
	public void add(final Message message, Object... params) {
		
		getLogger().debug(getBundle().getString("adding-message-to-context", message.toString()));
		if (params == null || params.length == 0) {
			getAppender().append(message);
		} else {
			getLogger().warn("Atualmente, ao chamar o método add do MessageContext passando um objeto" 
					+ " do tipo Message e mais parâmetros, será recriando um objeto" +"\n"
					+ " Message, na implementação DefaultMessage para que os parâmetros sejam utilizados."
					+ " Note que isso poderá trazer problemas para sua aplicação, caso" +"\n"
					+ " a implementação de Message utilizada não seja a DefaultMessage. Para evitar esse tipo de problema"
					+ " e garantir compatibilidade com versões futuras, recomendamos que" +"\n"
					+ " o objeto message seja criado com os parâmetros, e que para o método add apenas seja passado"
					+ " esse objeto como parâmetro.");
			
			getAppender().append(new DefaultMessage(message.getText(), message.getSeverity(), params));
		}
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
		throw new DemoiselleException(
				"Este método não é mais suportado desde a versão 2.4.0 do Demoiselle Framework. Considere atualizar a sua aplicação ou o componente com uma nova versão que faça uso do "
						+ MessageAppender.class.getCanonicalName() + ".");
	}

	@Override
	@Deprecated
	public void clear() {
		throw new DemoiselleException(
				"Este método não é mais suportado desde a versão 2.4.0 do Demoiselle Framework. Considere atualizar a sua aplicação ou o componente com uma nova versão que faça uso do "
						+ MessageAppender.class.getCanonicalName() + ".");
	}

	private  ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private  Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(MessageContext.class);
		}

		return logger;
	}
}
