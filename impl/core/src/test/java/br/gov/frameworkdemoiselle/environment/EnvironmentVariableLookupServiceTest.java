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
package br.gov.frameworkdemoiselle.environment;

import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.environment.spi.EnvironmentVariableResolver;
import br.gov.frameworkdemoiselle.mock.MockInitialContextFactory;

/**
 * Tests for {@link EnvironmentVariableResolverService}.
 */
public class EnvironmentVariableLookupServiceTest {

    /**
     * Class under test.
     */
    private EnvironmentVariableResolverService environmentVariableResolverService;

    /**
     * Prepare tests.
     */
    @Before
    public void setUp() {
        environmentVariableResolverService = new EnvironmentVariableResolverService();
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     */
    @Test
    public void testNoVariableResolverFound() {
        final String variableName = "wsdl-host";

        final Set<EnvironmentVariableResolver> emptyResolversSet = Collections.emptySet();
        Whitebox.setInternalState(environmentVariableResolverService, emptyResolversSet);
        assertNull(environmentVariableResolverService
                .getVariable(variableName));
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     */
    @Test
    public void testGetVariableNotFound() {
        final String variableNotExistent = "variable.does.not.exist";
        assertNull(environmentVariableResolverService
                .getVariable(variableNotExistent));
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     */
    @Test
    public void testGetVariableSystemProperty() {
        final String systemVariableName = "br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService.URL_SISTEMA";

        try {
            final String systemVariableValue = "http://aplic-desenv.serpro.gov.br";
            System.setProperty(systemVariableName, systemVariableValue);
            assertEquals(systemVariableValue,
                    environmentVariableResolverService
                            .getVariable(systemVariableName));
        } finally {
            System.clearProperty(systemVariableName);
        }
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     *
     * @throws NamingException
     *             error
     */
    @Test
    public void testGetVariableSimpleJndiProperty() throws NamingException {
        final String variableName = "jms-server-host";
        final String variableValue = "tcp://localhost:7222";

        final Context contextMock = createNiceControl().createMock(Context.class);
        expect(contextMock.lookup(variableName)).andReturn(
                variableValue).atLeastOnce();
        replay(contextMock);

        MockInitialContextFactory.setCurrentContext(contextMock);
        try {
            assertEquals(variableValue,
                    environmentVariableResolverService
                            .getVariable(variableName));
        } finally {
            MockInitialContextFactory.clearCurrentContext();
        }

        verify(contextMock);
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     *
     * @throws NamingException
     *             error
     */
    @Test
    public void testGetVariableTomcatSixJndiProperty() throws NamingException {
        final String variableName = "sso-logout-url";
        final String variableValue = "http://app-desenv.example.com/openam/UI/Logout";

        final String tomcatJndiVariableName = "java:comp/env/sso-logout-url";
        final Context contextMock = createNiceControl().createMock(Context.class);
        expect(contextMock.lookup(tomcatJndiVariableName)).andReturn(
                variableValue).atLeastOnce();
        replay(contextMock);

        MockInitialContextFactory.setCurrentContext(contextMock);
        try {
            assertEquals(variableValue,
                    environmentVariableResolverService
                            .getVariable(variableName));
        } finally {
            MockInitialContextFactory.clearCurrentContext();
        }

        verify(contextMock);
    }

    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     *
     * @throws NamingException
     *             error
     */
    @Test
    public void testGetVariableTomcatSevenJndiProperty() throws NamingException {
        final String variableName = "sso-login-url";
        final String variableValue = "http://app-desenv.example.com/openam/UI/Login";

        final String tomcatSevenJndiVariableName = "java:/comp/env/sso-login-url";
        final Context contextMock = createNiceControl().createMock(Context.class);
        expect(contextMock.lookup(tomcatSevenJndiVariableName)).andReturn(
                variableValue).atLeastOnce();
        replay(contextMock);

        MockInitialContextFactory.setCurrentContext(contextMock);
        try {
            assertEquals(variableValue,
                    environmentVariableResolverService
                            .getVariable(variableName));
        } finally {
            MockInitialContextFactory.clearCurrentContext();
        }

        verify(contextMock);
    }


    /**
     * Test method for
     * {@link br.gov.frameworkdemoiselle.environment.EnvironmentVariableResolverService#getVariable(java.lang.String)}
     * .
     * @throws NamingException error
     */
    @Test
    public void testRuntimeExceptionResolvingVariable() throws NamingException {
        final String variableName = "env-link";

        final Context contextMock = createNiceControl().createMock(Context.class);
        expect(contextMock.lookup(variableName)).andThrow(new RuntimeException("test!"));
        replay(contextMock);

        MockInitialContextFactory.setCurrentContext(contextMock);
        try {
            assertNull(environmentVariableResolverService
                            .getVariable(variableName));
        } finally {
            MockInitialContextFactory.clearCurrentContext();
        }

        verify(contextMock);
    }
}
