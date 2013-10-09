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
package br.gov.frameworkdemoiselle.internal.environment;

import java.util.Locale;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.environment.spi.EnvironmentVariableResolver;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Abstract class with common behavior when getting environment variables.
 */
public abstract class AbstractEnvironmentVariableResolver implements
        EnvironmentVariableResolver {

    /**
     * Message bundle name.
     */
    private static final String DEMOISELLE_CORE_BUNDLE_NAME = "demoiselle-core-bundle";

    /**
     * Message key.
     */
    private static final String MSG_RUNTIME_ERROR_RESOLVING_VARIABLE = "environment.variable.resolver.abstract.runtime-error-resolving-variable";

    /**
     * Resource bundle.
     */
    private static ResourceBundle bundle;

    /**
     * Logger.
     */
    private Logger logger;

    @Override
    public <T> T getVariable(String variableName) {
        try {
            return getVariableValue(variableName);
        } catch (final RuntimeException runtimeError) {
            getLogger().warn(
                    getBundle().getString(MSG_RUNTIME_ERROR_RESOLVING_VARIABLE, variableName),
                    runtimeError);
            return null;
        }
    }

    /**
     * Gets variable value.
     *
     * @param variableName
     *            variable name
     * @return variable value
     */
    protected abstract <T> T getVariableValue(String variableName);

    private Logger getLogger() {
        if (logger == null) {
            logger = LoggerProducer.create(getClass());
        }
        return logger;
    }

    private static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundleProducer.create(DEMOISELLE_CORE_BUNDLE_NAME, Locale.getDefault());
        }
        return bundle;
    }
}
