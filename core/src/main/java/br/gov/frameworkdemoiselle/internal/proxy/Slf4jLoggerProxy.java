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
/*
 * Demoiselle Framework Copyright (c) 2010 Serpro and other contributors as indicated by the @author tag. See the
 * copyright.txt in the distribution for a full listing of contributors. Demoiselle Framework is an open source Java EE
 * library designed to accelerate the development of transactional database Web applications. Demoiselle Framework is
 * released under the terms of the LGPL license 3 http://www.gnu.org/licenses/lgpl.html LGPL License 3 This file is part
 * of Demoiselle Framework. Demoiselle Framework is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License 3 as published by the Free Software Foundation. Demoiselle Framework
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You
 * should have received a copy of the GNU Lesser General Public License along with Demoiselle Framework. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package br.gov.frameworkdemoiselle.internal.proxy;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class Slf4jLoggerProxy implements Logger, Serializable {

	private static final long serialVersionUID = 1L;

	private transient final Logger delegate;

	public Slf4jLoggerProxy(final Logger logger) {
		this.delegate = logger;
	}

	@Override
	public void debug(final Marker marker, final String msg) {
		this.delegate.debug(marker, msg);
	}

	@Override
	public void debug(final Marker marker, final String format, final Object arg) {
		this.delegate.debug(marker, format, arg);
	}

	@Override
	public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
		this.delegate.debug(marker, format, arg1, arg2);
	}

	@Override
	public void debug(final Marker marker, final String format, final Object[] argArray) {
		this.delegate.debug(marker, format, argArray);
	}

	@Override
	public void debug(final Marker marker, final String msg, final Throwable t) {
		this.delegate.debug(marker, msg, t);
	}

	@Override
	public void debug(final String msg) {
		this.delegate.debug(msg);
	}

	@Override
	public void debug(final String format, final Object arg) {
		this.delegate.debug(format, arg);
	}

	@Override
	public void debug(final String format, final Object arg1, final Object arg2) {
		this.delegate.debug(format, arg1, arg2);
	}

	@Override
	public void debug(final String format, final Object[] argArray) {
		this.delegate.debug(format, argArray);
	}

	@Override
	public void debug(final String msg, final Throwable t) {
		this.delegate.debug(msg, t);
	}

	@Override
	public void error(final Marker marker, final String msg) {
		this.delegate.error(marker, msg);
	}

	@Override
	public void error(final Marker marker, final String format, final Object arg) {
		this.delegate.error(marker, format, arg);
	}

	@Override
	public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
		this.delegate.error(marker, format, arg1, arg2);
	}

	@Override
	public void error(final Marker marker, final String format, final Object[] argArray) {
		this.delegate.error(marker, format, argArray);
	}

	@Override
	public void error(final Marker marker, final String msg, final Throwable t) {
		this.delegate.error(marker, msg, t);
	}

	@Override
	public void error(final String msg) {
		this.delegate.error(msg);
	}

	@Override
	public void error(final String format, final Object arg) {
		this.delegate.error(format, arg);
	}

	@Override
	public void error(final String format, final Object arg1, final Object arg2) {
		this.delegate.error(format, arg1, arg2);
	}

	@Override
	public void error(final String format, final Object[] argArray) {
		this.delegate.error(format, argArray);
	}

	@Override
	public void error(final String msg, final Throwable t) {
		this.delegate.error(msg, t);
	}

	@Override
	public String getName() {
		return this.delegate.getName();
	}

	@Override
	public void info(final Marker marker, final String msg) {
		this.delegate.info(marker, msg);
	}

	@Override
	public void info(final Marker marker, final String format, final Object arg) {
		this.delegate.info(marker, format, arg);
	}

	@Override
	public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
		this.delegate.info(marker, format, arg1, arg2);
	}

	@Override
	public void info(final Marker marker, final String format, final Object[] argArray) {
		this.delegate.info(marker, format, argArray);
	}

	@Override
	public void info(final Marker marker, final String msg, final Throwable t) {
		this.delegate.info(marker, msg, t);
	}

	@Override
	public void info(final String msg) {
		this.delegate.info(msg);
	}

	@Override
	public void info(final String format, final Object arg) {
		this.delegate.info(format, arg);
	}

	@Override
	public void info(final String format, final Object arg1, final Object arg2) {
		this.delegate.info(format, arg1, arg2);
	}

	@Override
	public void info(final String format, final Object[] argArray) {
		this.delegate.info(format, argArray);
	}

	@Override
	public void info(final String msg, final Throwable t) {
		this.delegate.info(msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.delegate.isDebugEnabled();
	}

	@Override
	public boolean isDebugEnabled(final Marker marker) {
		return this.delegate.isDebugEnabled(marker);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.delegate.isErrorEnabled();
	}

	@Override
	public boolean isErrorEnabled(final Marker marker) {
		return this.delegate.isErrorEnabled(marker);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.delegate.isInfoEnabled();
	}

	@Override
	public boolean isInfoEnabled(final Marker marker) {
		return this.delegate.isInfoEnabled(marker);
	}

	@Override
	public boolean isTraceEnabled() {
		return this.delegate.isTraceEnabled();
	}

	@Override
	public boolean isTraceEnabled(final Marker marker) {
		return this.delegate.isTraceEnabled(marker);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.delegate.isWarnEnabled();
	}

	@Override
	public boolean isWarnEnabled(final Marker marker) {
		return this.delegate.isWarnEnabled(marker);
	}

	@Override
	public void trace(final Marker marker, final String msg) {
		this.delegate.trace(marker, msg);
	}

	@Override
	public void trace(final Marker marker, final String format, final Object arg) {
		this.delegate.trace(marker, format, arg);
	}

	@Override
	public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
		this.delegate.trace(marker, format, arg1, arg2);
	}

	@Override
	public void trace(final Marker marker, final String format, final Object[] argArray) {
		this.delegate.trace(marker, format, argArray);
	}

	@Override
	public void trace(final Marker marker, final String msg, final Throwable t) {
		this.delegate.trace(marker, msg, t);
	}

	@Override
	public void trace(final String msg) {
		this.delegate.trace(msg);
	}

	@Override
	public void trace(final String format, final Object arg) {
		this.delegate.trace(format, arg);
	}

	@Override
	public void trace(final String format, final Object arg1, final Object arg2) {
		this.delegate.trace(format, arg1, arg2);
	}

	@Override
	public void trace(final String format, final Object[] argArray) {
		this.delegate.trace(format, argArray);
	}

	@Override
	public void trace(final String msg, final Throwable t) {
		this.delegate.trace(msg, t);
	}

	@Override
	public void warn(final Marker marker, final String msg) {
		this.delegate.warn(marker, msg);
	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg) {
		this.delegate.warn(marker, format, arg);
	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
		this.delegate.warn(marker, format, arg1, arg2);
	}

	@Override
	public void warn(final Marker marker, final String format, final Object[] argArray) {
		this.delegate.warn(marker, format, argArray);
	}

	@Override
	public void warn(final Marker marker, final String msg, final Throwable t) {
		this.delegate.warn(marker, msg, t);
	}

	@Override
	public void warn(final String msg) {
		this.delegate.warn(msg);
	}

	@Override
	public void warn(final String format, final Object arg) {
		this.delegate.warn(format, arg);
	}

	@Override
	public void warn(final String format, final Object arg1, final Object arg2) {
		this.delegate.warn(format, arg1, arg2);
	}

	@Override
	public void warn(final String format, final Object[] argArray) {
		this.delegate.warn(format, argArray);
	}

	@Override
	public void warn(final String msg, final Throwable t) {
		this.delegate.warn(msg, t);
	}

}
