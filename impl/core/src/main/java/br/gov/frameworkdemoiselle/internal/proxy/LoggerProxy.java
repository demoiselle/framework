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
package br.gov.frameworkdemoiselle.internal.proxy;

import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerProxy extends Logger implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private transient Logger delegate;

	public LoggerProxy(String name) {
		super(name, null);
		this.name = name;
	}

	public Logger getDelegate() {
		if (this.delegate == null) {
			this.delegate = Logger.getLogger(this.name);
		}

		return this.delegate;
	}

	public int hashCode() {
		return getDelegate().hashCode();
	}

	public boolean equals(Object obj) {
		return getDelegate().equals(obj);
	}

	public String toString() {
		return getDelegate().toString();
	}

	public ResourceBundle getResourceBundle() {
		return getDelegate().getResourceBundle();
	}

	public String getResourceBundleName() {
		return getDelegate().getResourceBundleName();
	}

	public void setFilter(Filter newFilter) throws SecurityException {
		getDelegate().setFilter(newFilter);
	}

	public Filter getFilter() {
		return getDelegate().getFilter();
	}

	public void log(LogRecord record) {
		getDelegate().log(record);
	}

	public void log(Level level, String msg) {
		getDelegate().log(level, msg);
	}

	public void log(Level level, String msg, Object param1) {
		getDelegate().log(level, msg, param1);
	}

	public void log(Level level, String msg, Object[] params) {
		getDelegate().log(level, msg, params);
	}

	public void log(Level level, String msg, Throwable thrown) {
		getDelegate().log(level, msg, thrown);
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
		getDelegate().logp(level, sourceClass, sourceMethod, msg);
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
		getDelegate().logp(level, sourceClass, sourceMethod, msg, param1);
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
		getDelegate().logp(level, sourceClass, sourceMethod, msg, params);
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		getDelegate().logp(level, sourceClass, sourceMethod, msg, thrown);
	}

	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
		getDelegate().logrb(level, sourceClass, sourceMethod, bundleName, msg);
	}

	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) {
		getDelegate().logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
	}

	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
			Object[] params) {
		getDelegate().logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
	}

	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
			Throwable thrown) {
		getDelegate().logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
	}

	public void entering(String sourceClass, String sourceMethod) {
		getDelegate().entering(sourceClass, sourceMethod);
	}

	public void entering(String sourceClass, String sourceMethod, Object param1) {
		getDelegate().entering(sourceClass, sourceMethod, param1);
	}

	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		getDelegate().entering(sourceClass, sourceMethod, params);
	}

	public void exiting(String sourceClass, String sourceMethod) {
		getDelegate().exiting(sourceClass, sourceMethod);
	}

	public void exiting(String sourceClass, String sourceMethod, Object result) {
		getDelegate().exiting(sourceClass, sourceMethod, result);
	}

	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		getDelegate().throwing(sourceClass, sourceMethod, thrown);
	}

	public void severe(String msg) {
		getDelegate().severe(msg);
	}

	public void warning(String msg) {
		getDelegate().warning(msg);
	}

	public void info(String msg) {
		getDelegate().info(msg);
	}

	public void config(String msg) {
		getDelegate().config(msg);
	}

	public void fine(String msg) {
		getDelegate().fine(msg);
	}

	public void finer(String msg) {
		getDelegate().finer(msg);
	}

	public void finest(String msg) {
		getDelegate().finest(msg);
	}

	public void setLevel(Level newLevel) throws SecurityException {
		getDelegate().setLevel(newLevel);
	}

	public Level getLevel() {
		return getDelegate().getLevel();
	}

	public boolean isLoggable(Level level) {
		return getDelegate().isLoggable(level);
	}

	public String getName() {
		return getDelegate().getName();
	}

	public void addHandler(Handler handler) throws SecurityException {
		getDelegate().addHandler(handler);
	}

	public void removeHandler(Handler handler) throws SecurityException {
		getDelegate().removeHandler(handler);
	}

	public Handler[] getHandlers() {
		return getDelegate().getHandlers();
	}

	public void setUseParentHandlers(boolean useParentHandlers) {
		getDelegate().setUseParentHandlers(useParentHandlers);
	}

	public boolean getUseParentHandlers() {
		return getDelegate().getUseParentHandlers();
	}

	public Logger getParent() {
		return getDelegate().getParent();
	}

	public void setParent(Logger parent) {
		getDelegate().setParent(parent);
	}
}
