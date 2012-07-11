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
package br.gov.frameworkdemoiselle.internal.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.implementation.DefaultTransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.util.Reflections;

public class TransactionBootstrap extends AbstractBootstrap {

	private static Class<Transaction> selected = loadSelected();

	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {
		Class<?> annotated = event.getAnnotatedType().getJavaClass();

		if (Reflections.isOfType(annotated, Transaction.class) && annotated != loadSelected()) {
			event.veto();
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<Transaction> loadSelected() {
		synchronized (selected) {
			String canonicalName = null;

			try {
				Configuration config = new PropertiesConfiguration("demoiselle.properties");
				canonicalName = config.getString("frameworkdemoiselle.transaction.class",
						DefaultTransaction.class.getCanonicalName());

				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				selected = (Class<Transaction>) Class.forName(canonicalName, false, classLoader);
				selected.asSubclass(Transaction.class);

			} catch (org.apache.commons.configuration.ConfigurationException cause) {
				throw new ConfigurationException(getBundle().getString("file-not-found", "demoiselle.properties"));

			} catch (ClassNotFoundException cause) {
				throw new ConfigurationException(getBundle().getString("transaction-class-not-found", canonicalName));

			} catch (ClassCastException cause) {
				throw new ConfigurationException(getBundle().getString("transaction-class-must-be-of-type",
						canonicalName, Transaction.class.getCanonicalName()));
			}
		}

		return selected;
	}
}
