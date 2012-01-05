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
package br.gov.frameworkdemoiselle.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.lang.reflect.Field;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.annotation.Ignore;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Reflections.class })
public class StringsTest {

	@Test
	public void testGetString() {
		testEqualsGetString("teste", "teste");
		testEqualsGetString("", "");
		testEqualsGetString(null, null);
		testEqualsGetString(null, null, "teste");
		testEqualsGetString("{0}", "teste", "teste");
		testEqualsGetString("teste", "teste", (Object[]) null);
		testEqualsGetString("teste {0}", "teste 1", "1");
		testEqualsGetString("{0} teste", "Um teste", "Um");
		testEqualsGetString("{1} testando {0}", "Apenas testando novamente", "novamente", "Apenas");
		testEqualsGetString("{0} testando {1}", "Apenas testando {1}", "Apenas", null);
		testEqualsGetString("testando {1} novamente", "testando isto novamente", "aquilo", "isto");
		testEqualsGetString("teste", "teste", "1", "2");
		testEqualsGetString("teste {0}.", "teste \\.", "\\");
	}

	@Test
	public void testIsEmpty() {
		assertTrue(Strings.isEmpty(null));
		assertTrue(Strings.isEmpty(""));
		assertTrue(Strings.isEmpty(" "));
		assertTrue(Strings.isEmpty("                 "));

		assertFalse(Strings.isEmpty(" _ "));
		assertFalse(Strings.isEmpty("."));
		assertFalse(Strings.isEmpty("null"));
	}

	@Test
	public void testIsResourceBundleKeyFormat() {
		assertTrue(Strings.isResourceBundleKeyFormat("{x}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{.}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{*}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{$}"));
		assertFalse(Strings.isResourceBundleKeyFormat("{}"));
		assertFalse(Strings.isResourceBundleKeyFormat(""));
		assertFalse(Strings.isResourceBundleKeyFormat(" "));
		assertFalse(Strings.isResourceBundleKeyFormat(null));
		assertFalse(Strings.isResourceBundleKeyFormat("a{a}a"));
		assertFalse(Strings.isResourceBundleKeyFormat("a{a}"));
		assertFalse(Strings.isResourceBundleKeyFormat("{a}a"));
		assertFalse(Strings.isResourceBundleKeyFormat(" {a} "));
		assertFalse(Strings.isResourceBundleKeyFormat("{a"));
		assertFalse(Strings.isResourceBundleKeyFormat("a}"));
	}

	@Test
	public void testCamelCaseToSymbolSeparated() {
		assertEquals(null, Strings.camelCaseToSymbolSeparated(null, null));
		assertEquals(null, Strings.camelCaseToSymbolSeparated(null, "."));
		assertEquals("myvar", Strings.camelCaseToSymbolSeparated("myVar", null));
		assertEquals("myvar", Strings.camelCaseToSymbolSeparated("myVar", ""));
		assertEquals("my.var", Strings.camelCaseToSymbolSeparated("myVar", "."));
		assertEquals("my-var", Strings.camelCaseToSymbolSeparated("MyVar", "-"));
		assertEquals("my?var?name", Strings.camelCaseToSymbolSeparated("myVarName", "?"));
		assertEquals("my___var___name", Strings.camelCaseToSymbolSeparated("myVarName", "___"));
	}

	@Test
	public void testFirstToUpper() {
		assertNull(Strings.firstToUpper(null));
		assertEquals("", Strings.firstToUpper(""));
		assertEquals("A", Strings.firstToUpper("a"));
		assertEquals("A", Strings.firstToUpper("A"));
		assertEquals("Ab", Strings.firstToUpper("Ab"));
		assertEquals("AB", Strings.firstToUpper("AB"));
		assertEquals("Ab", Strings.firstToUpper("ab"));
		assertEquals("AB", Strings.firstToUpper("aB"));
	}
	
	@Test
	public void testToStringWhenObjectIsNull() {
		assertEquals("", Strings.toString(null));
	}
	
	@Test
	public void testToString() throws SecurityException, NoSuchFieldException {

		@SuppressWarnings("unused")
		class Test {

			private String name = "myName";

			private String lastname = "myLastname";

			private String nullField = null;

			@Ignore
			private String ignore = "ignoreMe";
			
		}

		mockStatic(Reflections.class);
		Test test = new Test();

		expect(Reflections.getNonStaticDeclaredFields(test.getClass())).andReturn(Test.class.getDeclaredFields());
		expect(Reflections.getFieldValue(EasyMock.anyObject(Field.class), EasyMock.anyObject())).andReturn("myName");
		expect(Reflections.getFieldValue(EasyMock.anyObject(Field.class), EasyMock.anyObject()))
				.andReturn("myLastname");
		expect(Reflections.getFieldValue(EasyMock.anyObject(Field.class), EasyMock.anyObject())).andReturn(null);
		expect(Reflections.getFieldValue(EasyMock.anyObject(Field.class), EasyMock.anyObject())).andReturn("Object");

		replayAll(Reflections.class);

		// FIXME Este this$0=Object não deveria aparecer!
		assertEquals("Test [name=myName, lastname=myLastname, nullField=null, this$0=Object]",
				Strings.toString(new Test()));

		verifyAll();
	}

	private void testEqualsGetString(String in, String expected, Object... params) {
		String out = Strings.getString(in, params);
		assertEquals(expected, out);
	}
	
	@Test
	public void testRemoveBraces() {
		assertNull(Strings.removeBraces(null));
		assertEquals("", Strings.removeBraces(""));
		assertEquals(" ", Strings.removeBraces(" "));
		assertEquals(" {x} ", Strings.removeBraces(" {x} "));
		assertEquals("{x} ", Strings.removeBraces("{x} "));
		assertEquals(" {x}", Strings.removeBraces(" {x}"));
		assertEquals("x", Strings.removeBraces("{x}"));
		assertEquals("a b c", Strings.removeBraces("{a b c}"));
		assertEquals("{}", Strings.removeBraces("{}"));
		assertEquals("{}", Strings.removeBraces("{{}}"));
		assertEquals("?", Strings.removeBraces("{?}"));
		assertEquals("*", Strings.removeBraces("{*}"));
	}
	
	@Test
	public void testInsertBraces() {
		assertNull(Strings.insertBraces(null));
		assertEquals("", Strings.insertBraces(""));
		assertEquals(" ", Strings.insertBraces(" "));
		assertEquals("{ x }", Strings.insertBraces(" x "));
		assertEquals("{ {x} }", Strings.insertBraces(" {x} "));
		assertEquals("{{x}", Strings.insertBraces("{x"));
		assertEquals("{*}", Strings.insertBraces("*"));
		assertEquals("{?}", Strings.insertBraces("?"));
	}
	
	@Test
	public void testRemoveCharsWhenStringIsNull() {
		assertEquals(null, Strings.removeChars(null, 'a'));
	}
	
	@Test
	public void testRemoveCharsWhenStringIsNotNull() {
		String string = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus lobortis.";
		string = Strings.removeChars(string, 'L', 'l');
		assertEquals(-1, string.indexOf('L'));
		assertEquals(-1, string.indexOf('l'));
	}
	
}
