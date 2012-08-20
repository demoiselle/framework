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
import org.junit.Ignore;
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
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBootstrap.class)
public class ConfigurationLoaderWithListTest {

	private ConfigurationLoader configurationLoader;

	@Configuration(type = ConfigType.PROPERTIES, resource = "configuration-with-list")
	public class ConfigurationPropertiesWithList {

		/*
		 * All methods supported by org.apache.commons.configuration.DataConfiguration class for List type
		 */

		protected List<BigDecimal> bigDecimalList;

		protected List<BigInteger> bigIntegerList;

		protected List<Boolean> booleanList;

		protected List<Byte> byteList;

		protected List<Calendar> calendarList;

		protected List<Color> colorList;

		protected List<Date> dateList;

		protected List<Double> doubleList;

		protected List<Float> floatList;

		protected List<Integer> integerList;

		protected List<Locale> localeList;

		protected List<Long> longList;

		protected List<Short> shortList;

		protected List<URL> urlList;

		protected List<String> stringList;
	}

	@Configuration(type = ConfigType.XML, resource = "configuration-with-list")
	public class ConfigurationXMLWithList {

		/*
		 * All methods supported by org.apache.commons.configuration.DataConfiguration class for List type
		 */

		protected List<BigDecimal> bigDecimalList;

		protected List<BigInteger> bigIntegerList;

		protected List<Boolean> booleanList;

		protected List<Byte> byteList;

		protected List<Calendar> calendarList;

		protected List<Color> colorList;

		protected List<Date> dateList;

		protected List<Double> doubleList;

		protected List<Float> floatList;

		protected List<Integer> integerList;

		protected List<Locale> localeList;

		protected List<Long> longList;

		protected List<Short> shortList;

		protected List<URL> urlList;

		protected List<String> stringList;
	}

	@Before
	public void setUp() throws Exception {
		Logger logger;
		ResourceBundle bundle;
		logger = PowerMock.createMock(Logger.class);
		bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		configurationLoader = new ConfigurationLoader();
		Whitebox.setInternalState(this.configurationLoader, "bundle", bundle);
		Whitebox.setInternalState(this.configurationLoader, "logger", logger);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConfigurationPropertiesWithIntegerList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Integer integerValue = config.integerList.get(0);

		assertEquals(Integer.class, integerValue.getClass());
		assertEquals(Integer.MAX_VALUE, integerValue.intValue());
		assertEquals(4, config.integerList.size());
	}

	@Test
	public void testConfigurationPropertiesWithShortList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Short shortValue = config.shortList.get(0);

		assertEquals(Short.class, shortValue.getClass());
		assertEquals(Short.MAX_VALUE, shortValue.shortValue());
		assertEquals(4, config.shortList.size());
	}

	@Test
	public void testConfigurationPropertiesWithByteList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Byte byteValue = config.byteList.get(0);

		assertEquals(Byte.class, byteValue.getClass());
		assertEquals(Byte.MAX_VALUE, byteValue.byteValue());
		assertEquals(8, config.byteList.size());
	}

	@Test
	public void testConfigurationPropertiesWithBooleanList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Boolean booleanValue = config.booleanList.get(0);

		assertEquals(Boolean.class, booleanValue.getClass());
		assertEquals(2, config.booleanList.size());
	}

	@Test
	public void testConfigurationPropertiesWithLongList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Long longValue = config.longList.get(0);

		assertEquals(Long.class, longValue.getClass());
		assertEquals(Long.MAX_VALUE, longValue.longValue());
		assertEquals(5, config.longList.size());
	}

	@Test
	public void testConfigurationPropertiesWithFloatList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Float floatValue = config.floatList.get(0);

		assertEquals(Float.class, floatValue.getClass());
		assertEquals(Float.MAX_VALUE, floatValue.floatValue(), 1);
		assertEquals(5, config.floatList.size());
	}

	@Test
	public void testConfigurationPropertiesWithDoubleList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Double doubleValue = config.doubleList.get(0);

		assertEquals(Double.class, doubleValue.getClass());
		assertEquals(Double.MAX_VALUE, doubleValue.doubleValue(), 1);
		assertEquals(3, config.doubleList.size());
	}

	@Test
	public void testConfigurationPropertiesWithBigDecimalList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		BigDecimal bigDecimalValue = config.bigDecimalList.get(0);

		assertEquals(BigDecimal.class, bigDecimalValue.getClass());
		assertEquals(3, config.bigDecimalList.size());
	}

	@Test
	public void testConfigurationPropertiesWithBigIntegerList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		BigInteger bigIntegerValue = config.bigIntegerList.get(0);

		assertEquals(BigInteger.class, bigIntegerValue.getClass());
		assertEquals(3, config.bigIntegerList.size());
	}

	@Test
	public void testConfigurationPropertiesWithCalendarList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Calendar calendarValue = config.calendarList.get(0);

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.JUNE, 14, 10, 10);

		assertEquals(Calendar.class, calendarValue.getClass().getSuperclass());
		assertEquals(calendar.getTimeInMillis(), calendarValue.getTimeInMillis());
		assertEquals(3, config.calendarList.size());
	}

	@Test
	public void testConfigurationPropertiesWithDateList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Date dateValue = config.dateList.get(0);

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.AUGUST, 14, 18, 10, 50);

		Date date = new Date(calendar.getTimeInMillis());

		assertEquals(Date.class, dateValue.getClass());
		assertEquals(date.getTime(), dateValue.getTime());
		assertEquals(3, config.dateList.size());
	}

	@Test
	public void testConfigurationPropertiesWithColorList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Color colorValue = config.colorList.get(0);

		assertEquals(Color.class, colorValue.getClass());
		assertEquals(Color.gray, colorValue);
		assertEquals(3, config.colorList.size());
	}

	@Test
	public void testConfigurationPropertiesWithLocaleList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		Locale localeValue = config.localeList.get(0);
		Locale localeValue2 = config.localeList.get(1);

		assertEquals(Locale.class, localeValue.getClass());
		assertEquals(Locale.ENGLISH, localeValue);
		assertEquals("BR", localeValue2.getCountry());
		assertEquals(3, config.localeList.size());
	}

	@Test
	public void testConfigurationPropertiesWithURLList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		URL urlValue = config.urlList.get(0);

		URL otherURL = null;

		try {
			otherURL = new URL("http://www.test.com");
		} catch (Exception e) {

		}

		assertEquals(URL.class, urlValue.getClass());
		assertEquals(otherURL, urlValue);
		assertEquals(3, config.urlList.size());
	}

	@Test
	public void testConfigurationPropertiesWithStringList() {
		ConfigurationPropertiesWithList config = prepareConfigurationPropertiesWithList();

		String stringValue = config.stringList.get(0);

		assertEquals(String.class, stringValue.getClass());
		assertEquals("Test", stringValue);
		assertEquals(3, config.stringList.size());
	}

	private ConfigurationPropertiesWithList prepareConfigurationPropertiesWithList() {
		ConfigurationPropertiesWithList config = new ConfigurationPropertiesWithList();

		mockStatic(CoreBootstrap.class);
		expect(CoreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replay(CoreBootstrap.class);

		configurationLoader.load(config);
		return config;
	}

	@Test
	public void testConfigurationXMLWithIntegerList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Integer integerValue = config.integerList.get(0);

		assertEquals(Integer.class, integerValue.getClass());
		assertEquals(Integer.MAX_VALUE, integerValue.intValue());
		assertEquals(4, config.integerList.size());
	}

	@Test
	public void testConfigurationXMLWithShortList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Short shortValue = config.shortList.get(0);

		assertEquals(Short.class, shortValue.getClass());
		assertEquals(Short.MAX_VALUE, shortValue.shortValue());
		assertEquals(4, config.shortList.size());
	}

	@Test
	public void testConfigurationXMLWithByteList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Byte byteValue = config.byteList.get(0);

		assertEquals(Byte.class, byteValue.getClass());
		assertEquals(Byte.MAX_VALUE, byteValue.byteValue());
		assertEquals(8, config.byteList.size());
	}

	@Test
	public void testConfigurationXMLWithBooleanList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Boolean booleanValue = config.booleanList.get(0);

		assertEquals(Boolean.class, booleanValue.getClass());
		assertEquals(2, config.booleanList.size());
	}

	@Test
	public void testConfigurationXMLWithLongList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Long longValue = config.longList.get(0);

		assertEquals(Long.class, longValue.getClass());
		assertEquals(Long.MAX_VALUE, longValue.longValue());
		assertEquals(5, config.longList.size());
	}

	@Test
	public void testConfigurationXMLWithFloatList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Float floatValue = config.floatList.get(0);

		assertEquals(Float.class, floatValue.getClass());
		assertEquals(Float.MAX_VALUE, floatValue.floatValue(), 1);
		assertEquals(5, config.floatList.size());
	}

	@Test
	public void testConfigurationXMLWithDoubleList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Double doubleValue = config.doubleList.get(0);

		assertEquals(Double.class, doubleValue.getClass());
		assertEquals(Double.MAX_VALUE, doubleValue.doubleValue(), 1);
		assertEquals(3, config.doubleList.size());
	}

	@Test
	public void testConfigurationXMLWithBigDecimalList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		BigDecimal bigDecimalValue = config.bigDecimalList.get(0);

		assertEquals(BigDecimal.class, bigDecimalValue.getClass());
		assertEquals(3, config.bigDecimalList.size());
	}

	@Test
	public void testConfigurationXMLWithBigIntegerList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		BigInteger bigIntegerValue = config.bigIntegerList.get(0);

		assertEquals(BigInteger.class, bigIntegerValue.getClass());
		assertEquals(3, config.bigIntegerList.size());
	}

	@Test
	public void testConfigurationXMLWithCalendarList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Calendar calendarValue = config.calendarList.get(0);

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.JUNE, 14, 10, 10);

		assertEquals(Calendar.class, calendarValue.getClass().getSuperclass());
		assertEquals(calendar.getTimeInMillis(), calendarValue.getTimeInMillis());
		assertEquals(3, config.calendarList.size());
	}

	@Test
	public void testConfigurationXMLWithDateList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Date dateValue = config.dateList.get(0);

		GregorianCalendar calendar = new GregorianCalendar(2012, Calendar.AUGUST, 14, 18, 10, 50);

		Date date = new Date(calendar.getTimeInMillis());

		assertEquals(Date.class, dateValue.getClass());
		assertEquals(date.getTime(), dateValue.getTime());
		assertEquals(3, config.dateList.size());
	}

	@Test
	public void testConfigurationXMLWithColorList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Color colorValue = config.colorList.get(0);

		assertEquals(Color.class, colorValue.getClass());
		assertEquals(Color.gray, colorValue);
		assertEquals(3, config.colorList.size());
	}

	@Test
	public void testConfigurationXMLWithLocaleList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		Locale localeValue = config.localeList.get(0);
		Locale localeValue2 = config.localeList.get(1);

		assertEquals(Locale.class, localeValue.getClass());
		assertEquals(Locale.ENGLISH, localeValue);
		assertEquals("BR", localeValue2.getCountry());
		assertEquals(3, config.localeList.size());
	}

	@Test
	public void testConfigurationXMLWithURLList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		URL urlValue = config.urlList.get(0);

		URL otherURL = null;

		try {
			otherURL = new URL("http://www.test.com");
		} catch (Exception e) {

		}

		assertEquals(URL.class, urlValue.getClass());
		assertEquals(otherURL, urlValue);
		assertEquals(3, config.urlList.size());
	}

	@Test
	public void testConfigurationXMLWithStringList() {
		ConfigurationXMLWithList config = prepareConfigurationXMLWithList();

		String stringValue = config.stringList.get(0);

		assertEquals(String.class, stringValue.getClass());
		assertEquals("Test", stringValue);
		assertEquals(3, config.stringList.size());
	}

	private ConfigurationXMLWithList prepareConfigurationXMLWithList() {
		ConfigurationXMLWithList config = new ConfigurationXMLWithList();

		mockStatic(CoreBootstrap.class);
		expect(CoreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replay(CoreBootstrap.class);

		configurationLoader.load(config);
		return config;
	}
}
