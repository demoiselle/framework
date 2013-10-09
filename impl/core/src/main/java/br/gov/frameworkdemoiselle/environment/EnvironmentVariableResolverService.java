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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.environment.spi.EnvironmentVariableResolver;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Environment variable resolver service.
 */
public class EnvironmentVariableResolverService {

    /**
     * Message bundle name.
     */
    private static final String DEMOISELLE_CORE_BUNDLE_NAME = "demoiselle-core-bundle";

    /**
     * Message key.
     */
    private static final String MSG_SERVICE_CONFIG_ERROR = "environment.variable.resolver.service.configuration-load-error";

    /**
     * Message key.
     */
    private static final String MSG_NO_VARIABLE_RESOLVER_FOUND = "environment.variable.resolver.service.no-resolver-found";

    /**
     * Message key.
     */
    private static final String MSG_VARIABLE_NOT_FOUND = "environment.variable.resolver.service.variable-not-found";

    /**
     * Resource bundle.
     */
    private static ResourceBundle bundle;

    /**
     * Logger.
     */
    private static Logger logger;

    /**
     * Variable resolvers.
     */
    private Set<EnvironmentVariableResolver> variableResolvers;

    /**
     * Returns variable's value.
     *
     * @param variableName
     *            variable name
     * @param <T>
     *            variable expected type
     * @return variable value or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String variableName) {
        final boolean noVariableResolversFound = getVariableResolvers().isEmpty();
        if (noVariableResolversFound) {
            logNoVariableResolverFound();
            return null;
        } else {
            return (T) resolveVariable(variableName);
        }
    }

    private Object resolveVariable(String variableName) {
        for (final EnvironmentVariableResolver variableResolver : getVariableResolvers()) {
            final Object variableValue = variableResolver
                    .getVariable(variableName);
            if (variableValue != null) {
                return variableValue;
            }
        }

        logVariableNotFound(variableName);
        return null;
    }

    private Set<EnvironmentVariableResolver> getVariableResolvers() {
        if (this.variableResolvers == null) {
            this.variableResolvers = lookupVariableResolvers();
        }
        return this.variableResolvers;
    }

    private Set<EnvironmentVariableResolver> lookupVariableResolvers() {
        final ServiceLoader<EnvironmentVariableResolver> serviceLoader = getServiceLoader();
        if (serviceLoader == null) {
            return Collections.emptySet();
        }

        final Set<EnvironmentVariableResolver> variableResolvers = new LinkedHashSet<EnvironmentVariableResolver>();
        final Iterator<EnvironmentVariableResolver> serviceLoaderIterator = serviceLoader
                .iterator();
        if (serviceLoaderIterator != null) {
            try {
                while (serviceLoaderIterator.hasNext()) {
                    final EnvironmentVariableResolver variableResolver = serviceLoaderIterator
                            .next();
                    variableResolvers.add(variableResolver);
                }
            } catch (final ServiceConfigurationError error) {
                getLogger().warn(
                        getBundle().getString(MSG_SERVICE_CONFIG_ERROR), error);
            }
        }

        return variableResolvers;
    }

    private void logNoVariableResolverFound() {
        getLogger().warn(getBundle().getString(MSG_NO_VARIABLE_RESOLVER_FOUND));
    }

    private void logVariableNotFound(String variableName) {
        getLogger().warn(
                getBundle().getString(MSG_VARIABLE_NOT_FOUND, variableName));
    }

    private ServiceLoader<EnvironmentVariableResolver> getServiceLoader() {
        return ServiceLoader.load(EnvironmentVariableResolver.class);
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = LoggerProducer
                    .create(EnvironmentVariableResolverService.class);
        }
        return logger;
    }

    private static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundleProducer.create(DEMOISELLE_CORE_BUNDLE_NAME,
                    Locale.getDefault());
        }
        return bundle;
    }
}
