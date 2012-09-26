//package br.gov.frameworkdemoiselle.internal.proxy;
//
//import java.io.Serializable;
//import java.util.Enumeration;
//
//import javax.enterprise.context.SessionScoped;
//import javax.enterprise.inject.Default;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpSession;
//import javax.servlet.http.HttpSessionContext;
//
//@Default
//@SessionScoped
//@SuppressWarnings("deprecation")
//public class HttpSessionProxy implements HttpSession, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private transient HttpSession delegate;
//	
//	public HttpSessionProxy(HttpSession delegate) {
//		this.delegate = delegate;
//	}
//
//	private HttpSession getDelegate() {
//		return delegate;
//	}
//
//	@Override
//	public long getCreationTime() {
//		return getDelegate().getCreationTime();
//	}
//
//	@Override
//	public String getId() {
//		return getDelegate().getId();
//	}
//
//	@Override
//	public long getLastAccessedTime() {
//		return getDelegate().getLastAccessedTime();
//	}
//
//	@Override
//	public ServletContext getServletContext() {
//		return getDelegate().getServletContext();
//	}
//
//	@Override
//	public void setMaxInactiveInterval(int interval) {
//		getDelegate().setMaxInactiveInterval(interval);
//	}
//
//	@Override
//	public int getMaxInactiveInterval() {
//		return getDelegate().getMaxInactiveInterval();
//	}
//
//	@Override
//	@Deprecated
//	public HttpSessionContext getSessionContext() {
//		return getDelegate().getSessionContext();
//	}
//
//	@Override
//	public Object getAttribute(String name) {
//		return getDelegate().getAttribute(name);
//	}
//
//	@Override
//	@Deprecated
//	public Object getValue(String name) {
//		return getDelegate().getValue(name);
//	}
//
//	@Override
//	public Enumeration<?> getAttributeNames() {
//		return getDelegate().getAttributeNames();
//	}
//
//	@Override
//	@Deprecated
//	public String[] getValueNames() {
//		return getDelegate().getValueNames();
//	}
//
//	@Override
//	public void setAttribute(String name, Object value) {
//		getDelegate().setAttribute(name, value);
//	}
//
//	@Override
//	@Deprecated
//	public void putValue(String name, Object value) {
//		getDelegate().putValue(name, value);
//	}
//
//	@Override
//	public void removeAttribute(String name) {
//		getDelegate().removeAttribute(name);
//	}
//
//	@Override
//	@Deprecated
//	public void removeValue(String name) {
//		getDelegate().removeValue(name);
//	}
//
//	@Override
//	public void invalidate() {
//		getDelegate().invalidate();
//	}
//
//	@Override
//	public boolean isNew() {
//		return getDelegate().isNew();
//	}
//}
