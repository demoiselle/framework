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
package logger;

import static junit.framework.Assert.assertEquals;

import java.util.logging.Logger;

import javax.inject.Inject;

import logger.appender.FakeHandler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class LoggerTest {

	@Inject
	private Logger unnamedLogger;

	@Inject
	@Name("just.another.test")
	private Logger namedLogger;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(LoggerTest.class);
		return deployment;
	}

	@Test
	public void unnamedLoggerProducer() {
		String message = "unnamed producer";

		FakeHandler handler = new FakeHandler();
		unnamedLogger.addHandler(handler);
		unnamedLogger.info(message);

		assertEquals(message, handler.getMessage());
		assertEquals(LoggerTest.class.getName(), handler.getName());
	}

	@Test
	public void namedLoggerProducer() {
		String message = "named producer";

		FakeHandler handler = new FakeHandler();
		namedLogger.addHandler(handler);
		namedLogger.info(message);

		assertEquals(message, handler.getMessage());
		assertEquals("just.another.test", handler.getName());
	}

	@Test
	public void loggerProducedByBeansGetReference() {
		String message = "beans reference producer";

		FakeHandler handler = new FakeHandler();
		Logger logger = Beans.getReference(Logger.class);
		logger.addHandler(handler);
		logger.info(message);

		assertEquals(message, handler.getMessage());
		assertEquals("not.categorized", handler.getName());
	}
}
