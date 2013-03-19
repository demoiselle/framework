///*
// * Demoiselle Framework
// * Copyright (C) 2010 SERPRO
// * ----------------------------------------------------------------------------
// * This file is part of Demoiselle Framework.
// * 
// * Demoiselle Framework is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License version 3
// * as published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License version 3
// * along with this program; if not,  see <http://www.gnu.org/licenses/>
// * or write to the Free Software Foundation, Inc., 51 Franklin Street,
// * Fifth Floor, Boston, MA  02110-1301, USA.
// * ----------------------------------------------------------------------------
// * Este arquivo é parte do Framework Demoiselle.
// * 
// * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
// * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
// * do Software Livre (FSF).
// * 
// * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
// * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
// * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
// * para maiores detalhes.
// * 
// * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
// * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
// * ou escreva para a Fundação do Software Livre (FSF) Inc.,
// * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
// */
//package br.gov.frameworkdemoiselle.internal.producer;
//
//import static org.easymock.EasyMock.createMock;
//import static org.easymock.EasyMock.expect;
//import static org.easymock.EasyMock.replay;
//import static org.junit.Assert.assertNotNull;
//
//import java.lang.reflect.Member;
//
//import javax.enterprise.inject.spi.InjectionPoint;
//
//import org.junit.Test;
//import org.slf4j.Logger;
//
//public class LoggerProducerTest {
//
//	private Logger logger;
//
//	@Test
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public void testCreateInjectionPoint() {
//
//		Member member = createMock(Member.class);
//		expect(member.getDeclaringClass()).andReturn((Class) this.getClass());
//		replay(member);
//
//		InjectionPoint injectionPoint = createMock(InjectionPoint.class);
//		expect(injectionPoint.getMember()).andReturn(member);
//		replay(injectionPoint);
//
//		logger = LoggerProducer.create(injectionPoint);
//		assertNotNull(logger);
//	}
//
//	@Test
//	public void testCreateWithNullInjectionPoint() {
//		logger = LoggerProducer.create((InjectionPoint) null);
//		assertNotNull(logger);
//	}
//
//	@Test
//	public void testCreateClass() {
//		logger = LoggerProducer.create(this.getClass());
//		assertNotNull(logger);
//	}
//
//	// We don't need to instantiate LoggerProducer class. But if we don't get in this way, we'll not get 100% on
//	// cobertura.
//	@Test
//	public void testLoggerFactoryDiferentNull() {
//		@SuppressWarnings("unused")
//		LoggerProducer loggerProducer = new LoggerProducer();
//	}
//
//}
