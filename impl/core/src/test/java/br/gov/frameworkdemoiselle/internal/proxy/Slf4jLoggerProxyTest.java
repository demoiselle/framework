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
package br.gov.frameworkdemoiselle.internal.proxy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggerFactory.class)
public class Slf4jLoggerProxyTest {

	private Logger logger;
	private Slf4jLoggerProxy slf4jLoggerProxy;
	
	@Before
	public void setUp() throws Exception {
		this.logger = EasyMock.createMock(Logger.class);
		this.slf4jLoggerProxy = new Slf4jLoggerProxy(Logger.class);
		
		mockStatic(LoggerFactory.class);
		
		expect(LoggerFactory.getLogger(EasyMock.anyObject(Class.class))).andReturn(logger);
	}
	
	@Test
	public void testDebugWithMarkerAndString() {
		Marker marker = null;
		this.logger.debug(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test
	public void testDebugWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.debug(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
//	@Test
//	public void testDebugWithMarkerStringAndTwoObjects() {
//		Marker marker = nullthis.slf4jLoggerProxy = new Slf4jLoggerProxy(Logger.class);;
//		Object obj1 = null, obj2 = null;
//		this.logger.debug(marker,"",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug(marker,"",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithMarkerStringAndObjectArray() {
//		Marker marker = null;
//		Object[] obj = null;
//		this.logger.debug(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug(marker,"",obj);
//		verify(this.logger);
//	}
//	
//	@Test
//	public void testDebugWithMarkerStringAndThrowable() {
//		Marker marker = null;
//		Throwable t = null;
//		this.logger.debug(marker,"",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug(marker,"",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithString() {
//		this.logger.debug("");
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug("");
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithStringAndOneObject() {
//		Object obj = null;
//		this.logger.debug("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithStringAndTwoObjects() {
//		Object obj1 = null, obj2 = null;
//		this.logger.debug("",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug("",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithStringAndObjectArray() {
//		Object[] obj = null;
//		this.logger.debug("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testDebugWithStringAndThrowable() {
//		Throwable t = null;
//		this.logger.debug("",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.debug("",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithMarkerAndString() {
//		Marker marker = null;
//		this.logger.error(marker,"");
//		replay(this.logger);
//		this.slf4jLoggerProxy.error(marker,"");
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithMarkerStringAndOneObject() {
//		Marker marker = null;
//		Object obj = null;
//		this.logger.error(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithMarkerStringAndTwoObjects() {
//		Marker marker = null;
//		Object obj1 = null, obj2 = null;
//		this.logger.error(marker,"",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error(marker,"",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithMarkerStringAndObjectArray() {
//		Marker marker = null;
//		Object[] obj1 = null;
//		this.logger.error(marker,"",obj1);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error(marker,"",obj1);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithMarkerStringAndThrowable() {
//		Marker marker = null;
//		Throwable t = null;
//		this.logger.error(marker,"",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error(marker,"",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithString() {
//		this.logger.error("");
//		replay(this.logger);
//		this.slf4jLoggerProxy.error("");
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithStringAndOneObject() {
//		Object obj = null;
//		this.logger.error("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithStringAndTwoObjects() {
//		Object obj1 = null,obj2 = null;
//		this.logger.error("",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error("",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithStringAndObjectArray() {
//		Object[] obj = null;
//		this.logger.error("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testErrorWithStringAndThrowable() {
//		Throwable t = null;
//		this.logger.error("",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.error("",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testGetName() {
//		expect(this.logger.getName()).andReturn("xxx");
//		replay(this.logger);
//		assertEquals("xxx", this.slf4jLoggerProxy.getName());
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithMarkerAndString() {
//		Marker marker = null;
//		this.logger.info(marker,"");
//		replay(this.logger);
//		this.slf4jLoggerProxy.info(marker,"");
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithMarkerStringAndOneObject() {
//		Marker marker = null;
//		Object obj = null;
//		this.logger.info(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithMarkerStringAndTwoObjects() {
//		Marker marker = null;
//		Object obj1 = null, obj2 = null;
//		this.logger.info(marker,"",obj1, obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info(marker,"",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithMarkerStringAndObjectArray() {
//		Marker marker = null;
//		Object[] obj = null;
//		this.logger.info(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithMarkerStringAndThrowable() {
//		Marker marker = null;
//		Throwable t = null;
//		this.logger.info(marker,"",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info(marker,"",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithString() {
//		this.logger.info("");
//		replay(this.logger);
//		this.slf4jLoggerProxy.info("");
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithStringAndOneObject() {
//		Object obj = null;
//		this.logger.info("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithStringAndTwoObjects() {
//		Object obj1 = null, obj2 = null;
//		this.logger.info("",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info("",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithStringAndObjectArray() {
//		Object[] obj = null;
//		this.logger.info("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testInfoWithStringAndThrowable() {
//		Throwable t = null;
//		this.logger.info("",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.info("",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testIsDebugEnabled() {
//		expect(this.logger.isDebugEnabled()).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isDebugEnabled());
//		verify(this.logger);
//	}
//	@Test
//	public void testIsDebugEnabledWithMarker() {
//		Marker marker = null;
//		expect(this.logger.isDebugEnabled(marker)).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isDebugEnabled(marker));
//		verify(this.logger);
//	}
//	@Test
//	public void testIsErrorEnabled() {
//		expect(this.logger.isErrorEnabled()).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isErrorEnabled());
//		verify(this.logger);
//	}
//	@Test
//	public void testIsErrorEnabledWithMarker() {
//		Marker marker = null;
//		expect(this.logger.isErrorEnabled(marker)).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isErrorEnabled(marker));
//		verify(this.logger);
//	}
//	@Test
//	public void testIsInfoEnabled() {
//		expect(this.logger.isInfoEnabled()).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isInfoEnabled());
//		verify(this.logger);
//	}
//	@Test
//	public void testIsInfoEnabledWithMarker() {
//		Marker marker = null;
//		expect(this.logger.isInfoEnabled(marker)).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isInfoEnabled(marker));
//		verify(this.logger);
//	}
//	@Test
//	public void testIsTRaceEnabled() {
//		expect(this.logger.isTraceEnabled()).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isTraceEnabled());
//		verify(this.logger);
//	}
//	@Test
//	public void testIsTraceEnabledWithMarker() {
//		Marker marker = null;
//		expect(this.logger.isTraceEnabled(marker)).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isTraceEnabled(marker));
//		verify(this.logger);
//	}
//	@Test
//	public void testIsWarnEnabled() {
//		expect(this.logger.isWarnEnabled()).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isWarnEnabled());
//		verify(this.logger);
//	}
//	@Test
//	public void testIsWarnEnabledWithMarker() {
//		Marker marker = null;
//		expect(this.logger.isWarnEnabled(marker)).andReturn(true);
//		replay(this.logger);
//		assertEquals(true, this.slf4jLoggerProxy.isWarnEnabled(marker));
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithMarkerAndString() {
//		Marker marker = null;
//		this.logger.trace(marker,"");
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace(marker,"");
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithMarkerStringAndOneObject() {
//		Marker marker = null;
//		Object obj = null;
//		this.logger.trace(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithMarkerStringAndTwoObjects() {
//		Marker marker = null;
//		Object obj1 = null, obj2 = null;
//		this.logger.trace(marker,"",obj1, obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace(marker,"",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithMarkerStringAndObjectArray() {
//		Marker marker = null;
//		Object[] obj = null;
//		this.logger.trace(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithMarkerStringAndThrowable() {
//		Marker marker = null;
//		Throwable t = null;
//		this.logger.trace(marker,"",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace(marker,"",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithString() {
//		this.logger.trace("");
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace("");
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithStringAndOneObject() {
//		Object obj = null;
//		this.logger.trace("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithStringAndTwoObjects() {
//		Object obj1 = null, obj2 = null;
//		this.logger.trace("",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace("",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithStringAndObjectArray() {
//		Object[] obj = null;
//		this.logger.trace("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testTraceWithStringAndThrowable() {
//		Throwable t = null;
//		this.logger.trace("",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.trace("",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithMarkerAndString() {
//		Marker marker = null;
//		this.logger.warn(marker,"");
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn(marker,"");
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithMarkerStringAndOneObject() {
//		Marker marker = null;
//		Object obj = null;
//		this.logger.warn(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithMarkerStringAndTwoObjects() {
//		Marker marker = null;
//		Object obj1 = null, obj2 = null;
//		this.logger.warn(marker,"",obj1, obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn(marker,"",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithMarkerStringAndObjectArray() {
//		Marker marker = null;
//		Object[] obj = null;
//		this.logger.warn(marker,"",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn(marker,"",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithMarkerStringAndThrowable() {
//		Marker marker = null;
//		Throwable t = null;
//		this.logger.warn(marker,"",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn(marker,"",t);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithString() {
//		this.logger.warn("");
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn("");
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithStringAndOneObject() {
//		Object obj = null;
//		this.logger.warn("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithStringAndTwoObjects() {
//		Object obj1 = null, obj2 = null;
//		this.logger.warn("",obj1,obj2);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn("",obj1,obj2);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithStringAndObjectArray() {
//		Object[] obj = null;
//		this.logger.warn("",obj);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn("",obj);
//		verify(this.logger);
//	}
//	@Test
//	public void testWarnWithStringAndThrowable() {
//		Throwable t = null;
//		this.logger.warn("",t);
//		replay(this.logger);
//		this.slf4jLoggerProxy.warn("",t);
//		verify(this.logger);
//	}
}
