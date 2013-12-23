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
package bootstraper;

import org.junit.Assert;
import org.junit.Test;

import br.gov.frameworkdemoiselle.Demoiselle;


public class DemoiselleSeBootstrapTest {

	@Test
	public void startThroughMainMethod(){
		Demoiselle.main(new String[]{"bootstraper.ClassWithMain" , "firstArgument" , "secondArgument" , "lastArgument"});
		
		Assert.assertEquals("firstArgument", ClassWithMain.firstArgument);
		Assert.assertEquals("lastArgument", ClassWithMain.lastArgument);
		Assert.assertEquals("injected resource data", ClassWithMain.dataFromInjectedResource);
		Assert.assertEquals("filled", ClassWithMain.startupData);
		Assert.assertEquals("filled", ClassWithMain.shutdownData);
	}

	@Test
	public void startThroughMainClassInterface(){
		Demoiselle.runStarterClass(ClassImplementingMainClass.class, new String[]{"firstArgument" , "secondArgument" , "lastArgument"});
		
		Assert.assertEquals("firstArgument", ClassImplementingMainClass.firstArgument);
		Assert.assertEquals("lastArgument", ClassImplementingMainClass.lastArgument);
		Assert.assertEquals("injected resource data", ClassImplementingMainClass.dataFromInjectedResource);
		Assert.assertEquals("filled", ClassImplementingMainClass.startupData);
		Assert.assertEquals("filled", ClassImplementingMainClass.shutdownData);
	}
	
	@Test
	public void startJFrame(){
		Demoiselle.runMainWindow(FakeJFrame.class);
		
		Assert.assertEquals("injected resource data", FakeJFrame.dataFromInjectedResource);
		Assert.assertEquals("dataFromPostConstruct", FakeJFrame.dataFromPostConstruct);
	}
}
