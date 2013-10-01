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
package util.beans.ambiguous;

import static junit.framework.Assert.assertEquals;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class AmbiguousBeansTest {

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(AmbiguousBeansTest.class);
		return deployment;
	}

	@Test
	public void failOnAmbiguousBeansImplementationsTest() {
		try {
			Beans.getReference(Bean.class);
		} catch (DemoiselleException cause) {
			assertEquals(AmbiguousResolutionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void failOnAmbiguousQualifiedBeansImplementationsTest() {
		try {
			Beans.getReference(Bean.class, new AnnotationLiteral<AmbiguousQualifier>() {

				private static final long serialVersionUID = 1L;
			});
		} catch (DemoiselleException cause) {
			assertEquals(AmbiguousResolutionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void ambiguityResolvedByQualifiersTest() {
		Bean bean;

		bean = Beans.getReference(Bean.class, new AnnotationLiteral<FirstValidQualifier>() {

			private static final long serialVersionUID = 1L;
		});
		assertEquals(FirstValidQualifiedBean.class, bean.getClass());

		bean = Beans.getReference(Bean.class, new AnnotationLiteral<SecondValidQualifier>() {

			private static final long serialVersionUID = 1L;
		});
		assertEquals(SecondValidQualifiedBean.class, bean.getClass());
	}
}
