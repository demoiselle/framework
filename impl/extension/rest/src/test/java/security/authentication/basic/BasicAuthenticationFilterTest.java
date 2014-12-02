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
package security.authentication.basic;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class BasicAuthenticationFilterTest {

	private static final String PATH = "src/test/resources/security/authentication/basic";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClasses(BasicAuthenticationFilterTest.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void loginSucessfull() throws Exception {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet get;
		HttpResponse response;
		int status;

		String username = "demoiselle";
		String password = "changeit";
		get = new HttpGet(deploymentUrl + "/helper");
		byte[] encoded = Base64.encodeBase64((username + ":" + password).getBytes());
		get.setHeader("Authorization", "Basic " + new String(encoded));
		response = client.execute(get);
		status = response.getStatusLine().getStatusCode();
		assertEquals(SC_OK, status);

		get = new HttpGet(deploymentUrl + "/helper");
		response = client.execute(get);
		status = response.getStatusLine().getStatusCode();
		assertEquals(SC_FORBIDDEN, status);
	}

	@Test
	public void loginFailed() throws Exception {
		String username = "invalid";
		String password = "invalid";

		HttpPost x = new HttpPost();
		x.setEntity(null);

		// HttpEntity entity

		HttpGet get = new HttpGet(deploymentUrl + "/helper");
		byte[] encoded = Base64.encodeBase64((username + ":" + password).getBytes());
		get.setHeader("Authorization", "Basic " + new String(encoded));

		HttpResponse response = HttpClientBuilder.create().build().execute(get);

		int status = response.getStatusLine().getStatusCode();
		assertEquals(SC_UNAUTHORIZED, status);
	}
}
