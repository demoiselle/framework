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
package br.gov.frameworkdemoiselle.internal.configuration;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class ConfigurationLoaderWithArrayTest {

	private ConfigurationLoader configurationLoader;

	@Configuration(type = ConfigType.PROPERTIES, resource = "configuration-with-array")
	public class ConfigurationPropertiesWithArray {

		/*
		 * All methods supported by org.apache.commons.configuration.DataConfiguration class for array
		 */

		protected BigDecimal[] bigDecimalArray;

		protected BigInteger[] bigIntegerArray;

		protected boolean[] booleanArray;

		protected byte[] byteArray;

		protected Calendar[] calendarArray;

		protected Color[] colorArray;

		protected Date[] dateArray;

		protected double[] doubleArray;

		protected float[] floatArray;

		protected int[] integerArray;

		protected Locale[] localeArray;

		protected long[] longArray;

		protected short[] shortArray;

		protected URL[] urlArray;

		protected String[] stringArray;

	}

	@Configuration(type = ConfigType.XML, resource = "configuration-with-array")
	public class ConfigurationXMLWithArray {

		/*
		 * All methods supported by org.apache.commons.configuration.DataConfiguration class for array
		 */

		protected BigDecimal[] bigDecimalArray;

		protected BigInteger[] bigIntegerArray;

		protected boolean[] booleanArray;

		protected byte[] byteArray;

		protected Calendar[] calendarArray;

		protected Color[] colorArray;

		protected Date[] dateArray;

		protected double[] doubleArray;

		protected float[] floatArray;

		protected int[] integerArray;

		protected Locale[] localeArray;

		protected long[] longArray;

		protected short[] shortArray;

		protected URL[] urlArray;

		protected String[] stringArray;

	}

	@Before
	public void setUp() throws Exception {
		configurationLoader = new ConfigurationLoader();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConfigurationPropertiesWithIntegerArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Integer integerValue = config.integerArray[0];

		assertEquals(Integer.class, integerValue.getClass());
		assertEquals(Integer.MAX_VALUE, integerValue.intValue());
		assertEquals(4, config.integerArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithShortArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Short shortValue = config.shortArray[0];

		assertEquals(Short.class, shortValue.getClass());
		assertEquals(Short.MAX_VALUE, shortValue.shortValue());
		assertEquals(4, config.shortArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithByteArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Byte byteValue = config.byteArray[0];

		assertEquals(Byte.class, byteValue.getClass());
		assertEquals(Byte.MAX_VALUE, byteValue.byteValue());
		assertEquals(8, config.byteArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithBooleanArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Boolean booleanValue = config.booleanArray[0];

		assertEquals(Boolean.class, booleanValue.getClass());
		assertEquals(2, config.booleanArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithLongArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Long longValue = config.longArray[0];

		assertEquals(Long.class, longValue.getClass());
		assertEquals(Long.MAX_VALUE, longValue.longValue());
		assertEquals(5, config.longArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithFloatArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Float floatValue = config.floatArray[0];

		assertEquals(Float.class, floatValue.getClass());
		assertEquals(Float.MAX_VALUE, floatValue.floatValue(), 1);
		assertEquals(5, config.floatArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithDoubleArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Double doubleValue = config.doubleArray[0];

		assertEquals(Double.class, doubleValue.getClass());
		assertEquals(Double.MAX_VALUE, doubleValue.doubleValue(), 1);
		assertEquals(3, config.doubleArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithBigDecimalArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		BigDecimal bigDecimalValue = config.bigDecimalArray[0];

		assertEquals(BigDecimal.class, bigDecimalValue.getClass());
		assertEquals(3, config.bigDecimalArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithBigIntegerArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		BigInteger bigIntegerValue = config.bigIntegerArray[0];

		assertEquals(BigInteger.class, bigIntegerValue.getClass());
		assertEquals(3, config.bigIntegerArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithCalendarArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Calendar calendarValue = config.calendarArray[0];

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.JUNE, 14, 10, 10);

		assertEquals(Calendar.class, calendarValue.getClass().getSuperclass());
		assertEquals(calendar.getTimeInMillis(), calendarValue.getTimeInMillis());
		assertEquals(3, config.calendarArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithDateArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Date dateValue = config.dateArray[0];

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.AUGUST, 14, 18, 10, 50);

		Date date = new Date(calendar.getTimeInMillis());

		assertEquals(Date.class, dateValue.getClass());
		assertEquals(date.getTime(), dateValue.getTime());
		assertEquals(3, config.dateArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithColorArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Color colorValue = config.colorArray[0];

		assertEquals(Color.class, colorValue.getClass());
		assertEquals(Color.gray, colorValue);
		assertEquals(3, config.colorArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithLocaleArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		Locale localeValue = config.localeArray[0];
		Locale localeValue2 = config.localeArray[1];

		assertEquals(Locale.class, localeValue.getClass());
		assertEquals(Locale.ENGLISH, localeValue);
		assertEquals("BR", localeValue2.getCountry());
		assertEquals(3, config.localeArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithURLArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		URL urlValue = config.urlArray[0];

		URL otherURL = null;

		try {
			otherURL = new URL("http://www.test.com");
		} catch (Exception e) {

		}

		assertEquals(URL.class, urlValue.getClass());
		assertEquals(otherURL, urlValue);
		assertEquals(3, config.urlArray.length);
	}

	@Test
	public void testConfigurationPropertiesWithStringArray() {
		ConfigurationPropertiesWithArray config = prepareConfigurationPropertiesWithArray();

		String stringValue = config.stringArray[0];

		assertEquals(String.class, stringValue.getClass());
		assertEquals("Test", stringValue);
		assertEquals(3, config.stringArray.length);
	}

	private ConfigurationPropertiesWithArray prepareConfigurationPropertiesWithArray() {
		mockStatic(Beans.class);
		ConfigurationPropertiesWithArray config = new ConfigurationPropertiesWithArray();
		CoreBootstrap coreBootstrap = PowerMock.createMock(CoreBootstrap.class);
		
		expect(Beans.getReference(CoreBootstrap.class)).andReturn(coreBootstrap);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());
		
		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);

		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		return config;
	}

	@Test
	public void testConfigurationXMLWithIntegerArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Integer integerValue = config.integerArray[0];

		assertEquals(Integer.class, integerValue.getClass());
		assertEquals(Integer.MAX_VALUE, integerValue.intValue());
		assertEquals(4, config.integerArray.length);
	}

	@Test
	public void testConfigurationXMLWithShortArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Short shortValue = config.shortArray[0];

		assertEquals(Short.class, shortValue.getClass());
		assertEquals(Short.MAX_VALUE, shortValue.shortValue());
		assertEquals(4, config.shortArray.length);
	}

	@Test
	public void testConfigurationXMLWithByteArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Byte byteValue = config.byteArray[0];

		assertEquals(Byte.class, byteValue.getClass());
		assertEquals(Byte.MAX_VALUE, byteValue.byteValue());
		assertEquals(8, config.byteArray.length);
	}

	@Test
	public void testConfigurationXMLWithBooleanArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Boolean booleanValue = config.booleanArray[0];

		assertEquals(Boolean.class, booleanValue.getClass());
		assertEquals(2, config.booleanArray.length);
	}

	@Test
	public void testConfigurationXMLWithLongArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Long longValue = config.longArray[0];

		assertEquals(Long.class, longValue.getClass());
		assertEquals(Long.MAX_VALUE, longValue.longValue());
		assertEquals(5, config.longArray.length);
	}

	@Test
	public void testConfigurationXMLWithFloatArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Float floatValue = config.floatArray[0];

		assertEquals(Float.class, floatValue.getClass());
		assertEquals(Float.MAX_VALUE, floatValue.floatValue(), 1);
		assertEquals(5, config.floatArray.length);
	}

	@Test
	public void testConfigurationXMLWithDoubleArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Double doubleValue = config.doubleArray[0];

		assertEquals(Double.class, doubleValue.getClass());
		assertEquals(Double.MAX_VALUE, doubleValue.doubleValue(), 1);
		assertEquals(3, config.doubleArray.length);
	}

	@Test
	public void testConfigurationXMLWithBigDecimalArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		BigDecimal bigDecimalValue = config.bigDecimalArray[0];

		assertEquals(BigDecimal.class, bigDecimalValue.getClass());
		assertEquals(3, config.bigDecimalArray.length);
	}

	@Test
	public void testConfigurationXMLWithBigIntegerArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		BigInteger bigIntegerValue = config.bigIntegerArray[0];

		assertEquals(BigInteger.class, bigIntegerValue.getClass());
		assertEquals(3, config.bigIntegerArray.length);
	}

	@Test
	public void testConfigurationXMLWithCalendarArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Calendar calendarValue = config.calendarArray[0];

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.JUNE, 14, 10, 10);

		assertEquals(Calendar.class, calendarValue.getClass().getSuperclass());
		assertEquals(calendar.getTimeInMillis(), calendarValue.getTimeInMillis());
		assertEquals(3, config.calendarArray.length);
	}

	@Test
	public void testConfigurationXMLWithDateArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Date dateValue = config.dateArray[0];

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.AUGUST, 14, 18, 10, 50);

		Date date = new Date(calendar.getTimeInMillis());

		assertEquals(Date.class, dateValue.getClass());
		assertEquals(date.getTime(), dateValue.getTime());
		assertEquals(3, config.dateArray.length);
	}

	@Test
	public void testConfigurationXMLWithColorArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Color colorValue = config.colorArray[0];

		assertEquals(Color.class, colorValue.getClass());
		assertEquals(Color.gray, colorValue);
		assertEquals(3, config.colorArray.length);
	}

	@Test
	public void testConfigurationXMLWithLocaleArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		Locale localeValue = config.localeArray[0];
		Locale localeValue2 = config.localeArray[1];

		assertEquals(Locale.class, localeValue.getClass());
		assertEquals(Locale.ENGLISH, localeValue);
		assertEquals("BR", localeValue2.getCountry());
		assertEquals(3, config.localeArray.length);
	}

	@Test
	public void testConfigurationXMLWithURLArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		URL urlValue = config.urlArray[0];

		URL otherURL = null;

		try {
			otherURL = new URL("http://www.test.com");
		} catch (Exception e) {

		}

		assertEquals(URL.class, urlValue.getClass());
		assertEquals(otherURL, urlValue);
		assertEquals(3, config.urlArray.length);
	}

	@Test
	public void testConfigurationXMLWithStringArray() {
		ConfigurationXMLWithArray config = prepareConfigurationXMLWithArray();

		String stringValue = config.stringArray[0];

		assertEquals(String.class, stringValue.getClass());
		assertEquals("Test", stringValue);
		assertEquals(3, config.stringArray.length);
	}

	private ConfigurationXMLWithArray prepareConfigurationXMLWithArray() {
		mockStatic(Beans.class);
		ConfigurationXMLWithArray config = new ConfigurationXMLWithArray();
		CoreBootstrap coreBootstrap = PowerMock.createMock(CoreBootstrap.class);
		
		expect(Beans.getReference(CoreBootstrap.class)).andReturn(coreBootstrap);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());
		
		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);

		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		return config;
	}
}
