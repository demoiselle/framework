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
package br.gov.frameworkdemoiselle.message;

import javax.enterprise.inject.Alternative;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * @author SERPRO
 */
@Alternative
public class DefaultMessage implements Message {

	private final String originalText;

	private String parsedText;

	private final SeverityType severity;

	private final Object[] params;

	private final ResourceBundle bundle;

	public static final SeverityType DEFAULT_SEVERITY = SeverityType.INFO;

	public DefaultMessage(String text, SeverityType severity, Object... params) {
		this.originalText = text;
		this.severity = (severity == null ? DEFAULT_SEVERITY : severity);
		this.params = params;
		this.bundle = Beans.getReference(ResourceBundle.class);
	}

	public DefaultMessage(String text, Object... params) {
		this(text, null, (Object[]) params);
	}

	public String getText() {
		initParsedText();
		return parsedText;
	}

	private void initParsedText() {
		if (parsedText == null) {
			if (Strings.isResourceBundleKeyFormat(originalText)) {
				parsedText = bundle.getString(Strings.removeBraces(originalText));

			} else if (originalText != null) {
				parsedText = new String(originalText);
			}

			parsedText = Strings.getString(parsedText, params);
		}
	}

	public SeverityType getSeverity() {
		return severity;
	}

	@Override
	public String toString() {
		initParsedText();
		return Strings.toString(this);
	}
}
