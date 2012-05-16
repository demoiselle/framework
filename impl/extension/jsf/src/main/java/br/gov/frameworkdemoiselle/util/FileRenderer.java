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
package br.gov.frameworkdemoiselle.util;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Defines the default behavior of the objects responsible for rendering files to the user.
 * 
 * @author SERPRO
 */
public interface FileRenderer extends Serializable {

	/**
	 * Renders a byteArray for display to the user.
	 * 
	 * @param byteArray Byte Array to be rendered.
	 * @param contentType 
	 * @param fileName
	 */
	void render(final byte[] byteArray, final ContentType contentType, final String fileName);

	/**
	 * Renders an inputStream for display to the user.
	 * 
	 * @param stream
	 * @param contentType
	 * @param fileName
	 */
	void render(final InputStream stream, final ContentType contentType, final String fileName);

	/**
	 * Renders a file for display to the user.
	 * 
	 * @param file
	 * @param contentType
	 * @param fileName
	 */
	void render(final File file, final ContentType contentType, final String fileName);

	/**
	 * File content type.
	 * 
	 * @author SERPRO
	 */
	public enum ContentType {
		CSV("text/plain"),
		HTML("text/html"),
		ODT("application/vnd.oasis.opendocument.text"),
		PDF("application/pdf"),
		RTF("application/rtf"),
		TXT("text/plain"),
		XLS("application/vnd.ms-excel");	
		
		private String contentType;	
		
		/**
		 * Constructor receiving the fields alias and content type.
		 * @param alias Alias of content type
		 * @param contentType Value of content type
		 */
		private ContentType(String contentType){
			this.contentType = contentType;
		}
		
		/**
		 * Return the content type of Type
		 * @return Content Type
		 */
		public String getContentType() {
			return contentType;
		}	
		
	}

}
