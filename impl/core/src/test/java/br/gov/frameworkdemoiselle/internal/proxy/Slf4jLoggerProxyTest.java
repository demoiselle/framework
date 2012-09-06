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
	
	@Test//1
	public void testDebugWithMarkerAndString() {
		Marker marker = null;
		this.logger.debug(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test//2
	public void testDebugWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.debug(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//3
	public void testDebugWithMarkerStringAndTwoObjects() {
		Marker marker = null;
		Object obj1 = null, obj2 = null;
		this.logger.debug(marker,"",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//4
	public void testDebugWithMarkerStringAndObjectArray() {
		Marker marker = null;
		Object[] obj = null;
		this.logger.debug(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//5
	public void testDebugWithMarkerStringAndThrowable() {
		Marker marker = null;
		Throwable t = null;
		this.logger.debug(marker,"",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug(marker,"",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//6
	public void testDebugWithString() {
		this.logger.debug("");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug("");
		PowerMock.verify(this.logger);
	}
	
	@Test//7
	public void testDebugWithStringAndOneObject() {
		Object obj = null;
		this.logger.debug("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//8
	public void testDebugWithStringAndTwoObjects() {
		Object obj1 = null, obj2 = null;
		this.logger.debug("",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug("",obj1,obj2);
		PowerMock.verify(this.logger);
	}

	@Test//9
	public void testDebugWithStringAndObjectArray() {
		Object[] obj = null;
		this.logger.debug("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//10
	public void testDebugWithStringAndThrowable() {
		Throwable t = null;
		this.logger.debug("",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.debug("",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//11
	public void testErrorWithMarkerAndString() {
		Marker marker = null;
		this.logger.error(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test//12
	public void testErrorWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.error(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error(marker,"",obj);
		PowerMock.verify(this.logger);
	}
		
	@Test//13
	public void testErrorWithMarkerStringAndTwoObjects() {
		Marker marker = null;
		Object obj1 = null, obj2 = null;
		this.logger.error(marker,"",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error(marker,"",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//14
	public void testErrorWithMarkerStringAndObjectArray() {
		Marker marker = null;
		Object[] obj1 = null;
		this.logger.error(marker,"",obj1);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error(marker,"",obj1);
		PowerMock.verify(this.logger);
	}
	
	@Test//15
	public void testErrorWithMarkerStringAndThrowable() {
		Marker marker = null;
		Throwable t = null;
		this.logger.error(marker,"",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error(marker,"",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//16
	public void testErrorWithString() {
		this.logger.error("");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error("");
		PowerMock.verify(this.logger);
	}
	
	@Test//17
	public void testErrorWithStringAndOneObject() {
		Object obj = null;
		this.logger.error("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//18
	public void testErrorWithStringAndTwoObjects() {
		Object obj1 = null,obj2 = null;
		this.logger.error("",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error("",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//19
	public void testErrorWithStringAndObjectArray() {
		Object[] obj = null;
		this.logger.error("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//20
	public void testErrorWithStringAndThrowable() {
		Throwable t = null;
		this.logger.error("",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.error("",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//21
	public void testGetName() {
		expect(this.logger.getName()).andReturn("xxx");
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals("xxx", this.slf4jLoggerProxy.getName());
		PowerMock.verify(this.logger);
	}
	
	@Test//22
	public void testInfoWithMarkerAndString() {
		Marker marker = null;
		this.logger.info(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test//23
	public void testInfoWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.info(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//24
	public void testInfoWithMarkerStringAndTwoObjects() {
		Marker marker = null;
		Object obj1 = null, obj2 = null;
		this.logger.info(marker,"",obj1, obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info(marker,"",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//25
	public void testInfoWithMarkerStringAndObjectArray() {
		Marker marker = null;
		Object[] obj = null;
		this.logger.info(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//26
	public void testInfoWithMarkerStringAndThrowable() {
		Marker marker = null;
		Throwable t = null;
		this.logger.info(marker,"",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info(marker,"",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//27
	public void testInfoWithString() {
		this.logger.info("");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info("");
		PowerMock.verify(this.logger);
	}
	
	@Test//28
	public void testInfoWithStringAndOneObject() {
		Object obj = null;
		this.logger.info("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//29
	public void testInfoWithStringAndTwoObjects() {
		Object obj1 = null, obj2 = null;
		this.logger.info("",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info("",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//30
	public void testInfoWithStringAndObjectArray() {
		Object[] obj = null;
		this.logger.info("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//31
	public void testInfoWithStringAndThrowable() {
		Throwable t = null;
		this.logger.info("",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.info("",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//32
	public void testIsDebugEnabled() {
		expect(this.logger.isDebugEnabled()).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isDebugEnabled());
		PowerMock.verify(this.logger);
	}
	
	@Test//33
	public void testIsDebugEnabledWithMarker() {
		Marker marker = null;
		expect(this.logger.isDebugEnabled(marker)).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isDebugEnabled(marker));
		PowerMock.verify(this.logger);
	}
	
	@Test//34
	public void testIsErrorEnabled() {
		expect(this.logger.isErrorEnabled()).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isErrorEnabled());
		PowerMock.verify(this.logger);
	}
	
	@Test//35
	public void testIsErrorEnabledWithMarker() {
		Marker marker = null;
		expect(this.logger.isErrorEnabled(marker)).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isErrorEnabled(marker));
		PowerMock.verify(this.logger);
	}
	
	@Test//36
	public void testIsInfoEnabled() {
		expect(this.logger.isInfoEnabled()).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isInfoEnabled());
		PowerMock.verify(this.logger);
	}
	
	@Test//37
	public void testIsInfoEnabledWithMarker() {
		Marker marker = null;
		expect(this.logger.isInfoEnabled(marker)).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isInfoEnabled(marker));
		PowerMock.verify(this.logger);
	}
	
	@Test//38
	public void testIsTRaceEnabled() {
		expect(this.logger.isTraceEnabled()).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isTraceEnabled());
		PowerMock.verify(this.logger);
	}
	
	@Test//39
	public void testIsTraceEnabledWithMarker() {
		Marker marker = null;
		expect(this.logger.isTraceEnabled(marker)).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isTraceEnabled(marker));
		PowerMock.verify(this.logger);
	}
	
	@Test//40
	public void testIsWarnEnabled() {
		expect(this.logger.isWarnEnabled()).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isWarnEnabled());
		PowerMock.verify(this.logger);
	}
	
	@Test//41
	public void testIsWarnEnabledWithMarker() {
		Marker marker = null;
		expect(this.logger.isWarnEnabled(marker)).andReturn(true);
		PowerMock.replay(LoggerFactory.class, this.logger);
		assertEquals(true, this.slf4jLoggerProxy.isWarnEnabled(marker));
		PowerMock.verify(this.logger);
	}
	
	@Test//42
	public void testTraceWithMarkerAndString() {
		Marker marker = null;
		this.logger.trace(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test//43
	public void testTraceWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.trace(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//44
	public void testTraceWithMarkerStringAndTwoObjects() {
		Marker marker = null;
		Object obj1 = null, obj2 = null;
		this.logger.trace(marker,"",obj1, obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace(marker,"",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//45
	public void testTraceWithMarkerStringAndObjectArray() {
		Marker marker = null;
		Object[] obj = null;
		this.logger.trace(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//46
	public void testTraceWithMarkerStringAndThrowable() {
		Marker marker = null;
		Throwable t = null;
		this.logger.trace(marker,"",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace(marker,"",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//47
	public void testTraceWithString() {
		this.logger.trace("");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace("");
		PowerMock.verify(this.logger);
	}
	
	@Test//48
	public void testTraceWithStringAndOneObject() {
		Object obj = null;
		this.logger.trace("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//49
	public void testTraceWithStringAndTwoObjects() {
		Object obj1 = null, obj2 = null;
		this.logger.trace("",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace("",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//50
	public void testTraceWithStringAndObjectArray() {
		Object[] obj = null;
		this.logger.trace("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//51
	public void testTraceWithStringAndThrowable() {
		Throwable t = null;
		this.logger.trace("",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.trace("",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//52
	public void testWarnWithMarkerAndString() {
		Marker marker = null;
		this.logger.warn(marker,"");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn(marker,"");
		PowerMock.verify(this.logger);
	}
	
	@Test//53
	public void testWarnWithMarkerStringAndOneObject() {
		Marker marker = null;
		Object obj = null;
		this.logger.warn(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//54
	public void testWarnWithMarkerStringAndTwoObjects() {
		Marker marker = null;
		Object obj1 = null, obj2 = null;
		this.logger.warn(marker,"",obj1, obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn(marker,"",obj1,obj2);
		PowerMock.verify(this.logger);
	}
	
	@Test//55
	public void testWarnWithMarkerStringAndObjectArray() {
		Marker marker = null;
		Object[] obj = null;
		this.logger.warn(marker,"",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn(marker,"",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//56
	public void testWarnWithMarkerStringAndThrowable() {
		Marker marker = null;
		Throwable t = null;
		this.logger.warn(marker,"",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn(marker,"",t);
		PowerMock.verify(this.logger);
	}
	
	@Test//57
	public void testWarnWithString() {
		this.logger.warn("");
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn("");
		PowerMock.verify(this.logger);
	}
	
	@Test//58
	public void testWarnWithStringAndOneObject() {
		Object obj = null;
		this.logger.warn("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//59
	public void testWarnWithStringAndTwoObjects() {
		Object obj1 = null, obj2 = null;
		this.logger.warn("",obj1,obj2);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn("",obj1,obj2);
		PowerMock.verify(this.logger);
	}
		
	@Test//60
	public void testWarnWithStringAndObjectArray() {
		Object[] obj = null;
		this.logger.warn("",obj);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn("",obj);
		PowerMock.verify(this.logger);
	}
	
	@Test//61
	public void testWarnWithStringAndThrowable() {
		Throwable t = null;
		this.logger.warn("",t);
		PowerMock.replay(LoggerFactory.class, this.logger);
		this.slf4jLoggerProxy.warn("",t);
		PowerMock.verify(this.logger);
	}
}
