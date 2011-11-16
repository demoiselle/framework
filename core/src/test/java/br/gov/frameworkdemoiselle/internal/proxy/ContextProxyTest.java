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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ContextProxyTest {

	private Context context;
	private ContextProxy contextProxy;
	
	@Before
	public void setUp() throws Exception {
		this.context = EasyMock.createMock(Context.class);
		this.contextProxy = new ContextProxy(this.context);
	}
	
	@Test
	public void testAddToEnvironment() throws NamingException {
		expect(this.context.addToEnvironment("", null)).andReturn(null);
		replay(this.context);
		this.contextProxy.addToEnvironment("", null);
		verify(this.context);
	}
	
	@Test
	public void testBindWithNameAndObject() throws NamingException {
		Name name = null;
		this.context.bind(name, null);
		replay(this.context);
		this.contextProxy.bind(name, null);
		verify(this.context);
	}
	
	@Test
	public void testBindWithStringAndObject() throws NamingException {
		this.context.bind("", null);
		replay(this.context);
		this.contextProxy.bind("", null);
		verify(this.context);
	}
	
	@Test
	public void testClose() throws NamingException {
		this.context.close();
		replay(this.context);
		this.contextProxy.close();
		verify(this.context);
	}
	
	@Test
	public void testComposeNameWithTwoNames() throws NamingException {
		Name n1=null, n2=null;
		expect(this.context.composeName(n1,n2)).andReturn(null);
		replay(this.context);
		this.contextProxy.composeName(n1,n2);
		verify(this.context);
	}
	
	@Test
	public void testComposeNameWithTwoStrings() throws NamingException {
		expect(this.context.composeName("","")).andReturn(null);
		replay(this.context);
		this.contextProxy.composeName("","");
		verify(this.context);
	}
	
	@Test
	public void testCreateSubcontextWithName() throws NamingException {
		Name n1 = null;
		expect(this.context.createSubcontext(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.createSubcontext(n1);
		verify(this.context);
	}
	
	@Test
	public void testCreateSubcontextWithString() throws NamingException {
		expect(this.context.createSubcontext("")).andReturn(null);
		replay(this.context);
		this.contextProxy.createSubcontext("");
		verify(this.context);
	}
	
	@Test
	public void testDestroySubcontextWithName() throws NamingException {
		Name n1=null;
		context.destroySubcontext(n1);
		replay(this.context);
		this.contextProxy.destroySubcontext(n1);
		verify(this.context);
	}
	
	@Test
	public void testDestroySubcontextWithString() throws NamingException {
		context.destroySubcontext("");
		replay(this.context);
		this.contextProxy.destroySubcontext("");
		verify(this.context);
	}
	
	@Test
	public void testGetNameInNamespace() throws NamingException {
		expect(this.context.getNameInNamespace()).andReturn(null);
		replay(this.context);
		this.contextProxy.getNameInNamespace();
		verify(this.context);
	}
	
	@Test
	public void testGetNameParserWithName() throws NamingException {
		Name n1=null;
		expect(this.context.getNameParser(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.getNameParser(n1);
		verify(this.context);
	}
	
	@Test
	public void testGetNameParserWithString() throws NamingException {
		expect(this.context.getNameParser("")).andReturn(null);
		replay(this.context);
		this.contextProxy.getNameParser("");
		verify(this.context);
	}
	
	@Test
	public void testListWithName() throws NamingException {
		Name n1=null;
		expect(this.context.list(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.list(n1);
		verify(this.context);
	}
	
	@Test
	public void testListWithString() throws NamingException {
		expect(this.context.list("")).andReturn(null);
		replay(this.context);
		this.contextProxy.list("");
		verify(this.context);
	}
	
	@Test
	public void testListBindingsWithName() throws NamingException {
		Name n1=null;
		expect(this.context.listBindings(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.listBindings(n1);
		verify(this.context);
	}
	
	@Test
	public void testListBindingsWithString() throws NamingException {
		expect(this.context.listBindings("")).andReturn(null);
		replay(this.context);
		this.contextProxy.listBindings("");
		verify(this.context);
	}
	
	@Test
	public void testLookupBindingsWithString() throws NamingException {
		Name n1 = null;
		expect(this.context.lookup(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.lookup(n1);
		verify(this.context);
	}
	
	@Test
	public void testLookupWithString() throws NamingException {
		expect(this.context.lookup("")).andReturn(null);
		replay(this.context);
		this.contextProxy.lookup("");
		verify(this.context);
	}
	
	@Test
	public void testLookupLinkBindingsWithString() throws NamingException {
		Name n1 = null;
		expect(this.context.lookupLink(n1)).andReturn(null);
		replay(this.context);
		this.contextProxy.lookupLink(n1);
		verify(this.context);
	}
	
	@Test
	public void testLookupLinkWithString() throws NamingException {
		expect(this.context.lookupLink("")).andReturn(null);
		replay(this.context);
		this.contextProxy.lookupLink("");
		verify(this.context);
	}
	
	@Test
	public void testRebindWithNameAndObject() throws NamingException {
		Name n1 = null;
		this.context.rebind(n1,null);
		replay(this.context);
		this.contextProxy.rebind(n1,null);
		verify(this.context);
	}
	
	@Test
	public void testRebindWithStringAndObject() throws NamingException {
		this.context.rebind("",null);
		replay(this.context);
		this.contextProxy.rebind("",null);
		verify(this.context);
	}
	
	@Test
	public void testRemoveFromEnvironmentWithString() throws NamingException {
		expect(this.context.removeFromEnvironment("")).andReturn(null);
		replay(this.context);
		this.contextProxy.removeFromEnvironment("");
		verify(this.context);
	}
	
	@Test
	public void testRenameWithTwoNames() throws NamingException {
		Name n1 = null, n2=null;
		this.context.rename(n1,n2);
		replay(this.context);
		this.contextProxy.rename(n1,n2);
		verify(this.context);
	}
	
	@Test
	public void testRenameWithTwoStrings() throws NamingException{
		this.context.rename("","");
		replay(this.context);
		this.contextProxy.rename("","");
		verify(this.context);
	}
	
	@Test
	public void testUnbind() throws NamingException {
		Name n1 = null;
		this.context.unbind(n1);
		replay(this.context);
		this.contextProxy.unbind(n1);
		verify(this.context);
	}
	
	@Test
	public void testUnbindWithString() throws NamingException {
		this.context.unbind("");
		replay(this.context);
		this.contextProxy.unbind("");
		verify(this.context);
	}
	
	@Test
	public void testGetEnvironment() throws NamingException {
		expect(this.context.getEnvironment()).andReturn(null);
		replay(this.context);
		this.contextProxy.getEnvironment();
		verify(this.context);
	}
}
