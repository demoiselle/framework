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
package br.gov.frameworkdemoiselle.internal.implementation;

import static java.util.logging.Level.WARNING;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.FileRenderer;

/**
 * Responsible for displaying the contents of files in the browser.
 */
public class FileRendererImpl implements FileRenderer {

	private static final long serialVersionUID = 7787266586182058798L;

	@Inject
	private HttpServletResponse response;

	@Inject
	private Logger logger;

	@Inject
	private FacesContext context;

	@Override
	public void render(final byte[] byteArray, final ContentType contentType, final String fileName,
			boolean forceDownload) {
		logger.fine("Renderizando para o arquivo " + fileName + ".");

		try {
			response.setContentType(contentType.getContentType());
			response.setContentLength(byteArray.length);

			String forceDownloadCommand = forceDownload ? "attachment; " : "";
			response.setHeader("Content-Disposition", forceDownloadCommand + "filename=\"" + fileName + "\"");

			logger.fine("Escrevendo o arquivo " + fileName + " no response.");
			response.getOutputStream().write(byteArray, 0, byteArray.length);
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			logger.log(WARNING, "Erro na geração do relatório. Incluíndo a exceção de erro em um FacesMessage", e);
			Faces.addMessage(e);
		}
		context.responseComplete();
	}

	@Override
	public void render(final byte[] byteArray, final ContentType contentType, final String fileName) {
		render(byteArray, contentType, fileName, false);
	}

	@Override
	public void render(final InputStream stream, final ContentType contentType, final String fileName,
			boolean forceDownload) {
		logger.fine("Renderizando o arquivo " + fileName + ".");
		render(getBytes(stream), contentType, fileName, forceDownload);
	}

	@Override
	public void render(final InputStream stream, final ContentType contentType, final String fileName) {
		render(stream, contentType, fileName, false);
	}

	@Override
	public void render(File file, ContentType contentType, String fileName, boolean forceDownload) {
		logger.fine("Renderizando para o arquivo " + fileName + ".");
		try {
			render(new FileInputStream(file), contentType, fileName, forceDownload);
		} catch (FileNotFoundException e) {
			logger.log(WARNING, "Erro na geração do relatório. Incluíndo a exceção de erro em um FacesMessage", e);
			Faces.addMessage(e);
		}
	}

	@Override
	public void render(File file, ContentType contentType, String fileName) {
		render(file, contentType, fileName, false);
	}

	private byte[] getBytes(InputStream stream) {
		byte[] byteArray = null;
		try {
			int thisLine;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while ((thisLine = stream.read()) != -1) {
				bos.write(thisLine);
			}
			bos.flush();
			byteArray = bos.toByteArray();

			if (bos != null) {
				bos.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return byteArray;
	}

}
