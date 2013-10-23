/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como publicada pela Fundação
 * do Software Livre (FSF); na versão 2 da Licença.
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/GPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse o Portal do Software Público
 * Brasileiro no endereço www.softwarepublico.gov.br ou escreva para a Fundação do Software
 * Livre (FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Utility class to configure the Locale.
 * 
 * @author SERPRO
 * */
@Named
@SessionScoped
public class Locales implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Locale PT_BR = new Locale("pt", "BR");
	
	private Locale locale = Locale.getDefault();

	@Inject
	private FacesContext facesContext;

	/**
	 * Set the language to "en_US". This is a shorthand to <code>setLocale(Locale.US)</code>.
	 */
	public void setEnglish() {
		setLocale(Locale.US);
	}

	/**
	 * Set the language to "pt_BR". This is a shorthand to <code>setLocale(Locales.PT_BR)</code>.
	 */
	public void setPortuguese() {
		setLocale(PT_BR);
	}
	
	/**
	 * @return The current locale, or {@link Locale#getDefault()} if one has not been set.
	 */
	public Locale getLocale(){
		return this.locale!=null ? this.locale : Locale.getDefault();
	}

	/**
	 * Set the locale for the current view
	 * 
	 * @param locale The new locale
	 */
	public void setLocale(Locale locale) {
		Iterator<Locale> supportedLocales = getContext().getApplication().getSupportedLocales();
		if (supportedLocales==null){
			this.locale = locale;
			getContext().getViewRoot().setLocale(this.locale);
		}
		else{
			boolean selectedLocale = false;
			while(supportedLocales.hasNext()){
				Locale supportedLocale = supportedLocales.next();
				if (supportedLocale.equals(locale)){
					this.locale = locale;
					getContext().getViewRoot().setLocale(this.locale);
					selectedLocale = true;
					break;
				}
			}
			
			if (!selectedLocale && this.locale==null){
				this.locale = Locale.getDefault();
			}
		}
	}
	
	/**
	 * Set the default locale for the entire application. After this call
	 * all views from this application will use this locale (unless a specific
	 * session defined a different locale using {@link #setLocale(Locale locale)}).
	 * 
	 * @param locale The locale to set
	 */
	public void setApplicationLocale(Locale locale) {
		setLocale(locale);
		getContext().getApplication().setDefaultLocale(this.locale);
	}
	
	private FacesContext getContext(){
		if (facesContext==null){
			facesContext = Beans.getReference(FacesContext.class);
		}
		
		return facesContext;
	}
}
