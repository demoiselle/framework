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
package management.testclasses;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import br.gov.frameworkdemoiselle.internal.management.qualifier.AttributeChange;
import br.gov.frameworkdemoiselle.internal.management.qualifier.Generic;
import br.gov.frameworkdemoiselle.management.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.ManagementNotificationEvent;
import br.gov.frameworkdemoiselle.management.NotificationManager;

/**
 * Dummy class to test receiving of notifications sent by the {@link NotificationManager} 
 * 
 * @author serpro
 *
 */
@ApplicationScoped
public class DummyNotificationListener {
	
	private String message = null;
	
	public void listenNotification(@Observes @Generic ManagementNotificationEvent event){
		message = event.getNotification().getMessage().toString();
	}
	
	public void listenAttributeChangeNotification(@Observes @AttributeChange ManagementNotificationEvent event){
		AttributeChangeNotification notification = (AttributeChangeNotification)event.getNotification();
		message = notification.getMessage().toString() + " - " + notification.getAttributeName();
	}
	
	public String getMessage() {
		return message;
	}
}
