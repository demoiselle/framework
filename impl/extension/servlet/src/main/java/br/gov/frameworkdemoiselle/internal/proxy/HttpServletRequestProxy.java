//package br.gov.frameworkdemoiselle.internal.proxy;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.Serializable;
//import java.io.UnsupportedEncodingException;
//import java.security.Principal;
//import java.util.Enumeration;
//import java.util.Locale;
//import java.util.Map;
//
//import javax.enterprise.context.RequestScoped;
//import javax.enterprise.inject.Default;
//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletInputStream;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//@Default
//@RequestScoped
//public class HttpServletRequestProxy implements HttpServletRequest, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private transient HttpServletRequest delegate;
//	
//	public HttpServletRequestProxy(HttpServletRequest delegate) {
//		this.delegate = delegate;
//	}
//
//	private HttpServletRequest getDelegate() {
//		return delegate;
//	}
//
//	@Override
//	public Object getAttribute(String name) {
//		return getDelegate().getAttribute(name);
//	}
//
//	@Override
//	public Enumeration<?> getAttributeNames() {
//		return getDelegate().getAttributeNames();
//	}
//
//	@Override
//	public String getCharacterEncoding() {
//		return getDelegate().getCharacterEncoding();
//	}
//
//	@Override
//	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
//		getDelegate().setCharacterEncoding(env);
//	}
//
//	@Override
//	public int getContentLength() {
//		return getDelegate().getContentLength();
//	}
//
//	@Override
//	public String getContentType() {
//		return getDelegate().getContentType();
//	}
//
//	@Override
//	public ServletInputStream getInputStream() throws IOException {
//		return getDelegate().getInputStream();
//	}
//
//	@Override
//	public String getParameter(String name) {
//		return getDelegate().getParameter(name);
//	}
//
//	@Override
//	public Enumeration<?> getParameterNames() {
//		return getDelegate().getParameterNames();
//	}
//
//	@Override
//	public String[] getParameterValues(String name) {
//		return getDelegate().getParameterValues(name);
//	}
//
//	@Override
//	public Map<?, ?> getParameterMap() {
//		return getDelegate().getParameterMap();
//	}
//
//	@Override
//	public String getProtocol() {
//		return getDelegate().getProtocol();
//	}
//
//	@Override
//	public String getScheme() {
//		return getDelegate().getScheme();
//	}
//
//	@Override
//	public String getServerName() {
//		return getDelegate().getServerName();
//	}
//
//	@Override
//	public int getServerPort() {
//		return getDelegate().getServerPort();
//	}
//
//	@Override
//	public BufferedReader getReader() throws IOException {
//		return getDelegate().getReader();
//	}
//
//	@Override
//	public String getRemoteAddr() {
//		return getDelegate().getRemoteAddr();
//	}
//
//	@Override
//	public String getRemoteHost() {
//		return getDelegate().getRemoteHost();
//	}
//
//	@Override
//	public void setAttribute(String name, Object o) {
//		getDelegate().setAttribute(name, o);
//	}
//
//	@Override
//	public void removeAttribute(String name) {
//		getDelegate().removeAttribute(name);
//	}
//
//	@Override
//	public Locale getLocale() {
//		return getDelegate().getLocale();
//	}
//
//	@Override
//	public Enumeration<?> getLocales() {
//		return getDelegate().getLocales();
//	}
//
//	@Override
//	public boolean isSecure() {
//		return getDelegate().isSecure();
//	}
//
//	@Override
//	public RequestDispatcher getRequestDispatcher(String path) {
//		return getDelegate().getRequestDispatcher(path);
//	}
//
//	@Override
//	@Deprecated
//	public String getRealPath(String path) {
//		return getDelegate().getRealPath(path);
//	}
//
//	@Override
//	public int getRemotePort() {
//		return getDelegate().getRemotePort();
//	}
//
//	@Override
//	public String getLocalName() {
//		return getDelegate().getLocalName();
//	}
//
//	@Override
//	public String getLocalAddr() {
//		return getDelegate().getLocalAddr();
//	}
//
//	@Override
//	public int getLocalPort() {
//		return getDelegate().getLocalPort();
//	}
//
//	@Override
//	public String getAuthType() {
//		return getDelegate().getAuthType();
//	}
//
//	@Override
//	public Cookie[] getCookies() {
//		return getDelegate().getCookies();
//	}
//
//	@Override
//	public long getDateHeader(String name) {
//		return getDelegate().getDateHeader(name);
//	}
//
//	@Override
//	public String getHeader(String name) {
//		return getDelegate().getHeader(name);
//	}
//
//	@Override
//	public Enumeration<?> getHeaders(String name) {
//		return getDelegate().getHeaders(name);
//	}
//
//	@Override
//	public Enumeration<?> getHeaderNames() {
//		return getDelegate().getHeaderNames();
//	}
//
//	@Override
//	public int getIntHeader(String name) {
//		return getDelegate().getIntHeader(name);
//	}
//
//	@Override
//	public String getMethod() {
//		return getDelegate().getMethod();
//	}
//
//	@Override
//	public String getPathInfo() {
//		return getDelegate().getPathInfo();
//	}
//
//	@Override
//	public String getPathTranslated() {
//		return getDelegate().getPathTranslated();
//	}
//
//	@Override
//	public String getContextPath() {
//		return getDelegate().getContextPath();
//	}
//
//	@Override
//	public String getQueryString() {
//		return getDelegate().getQueryString();
//	}
//
//	@Override
//	public String getRemoteUser() {
//		return getDelegate().getRemoteUser();
//	}
//
//	@Override
//	public boolean isUserInRole(String role) {
//		return getDelegate().isUserInRole(role);
//	}
//
//	@Override
//	public Principal getUserPrincipal() {
//		return getDelegate().getUserPrincipal();
//	}
//
//	@Override
//	public String getRequestedSessionId() {
//		return getDelegate().getRequestedSessionId();
//	}
//
//	@Override
//	public String getRequestURI() {
//		return getDelegate().getRequestURI();
//	}
//
//	@Override
//	public StringBuffer getRequestURL() {
//		return getDelegate().getRequestURL();
//	}
//
//	@Override
//	public String getServletPath() {
//		return getDelegate().getServletPath();
//	}
//
//	@Override
//	public HttpSession getSession(boolean create) {
//		return getDelegate().getSession(create);
//	}
//
//	@Override
//	public HttpSession getSession() {
//		return getDelegate().getSession();
//	}
//
//	@Override
//	public boolean isRequestedSessionIdValid() {
//		return getDelegate().isRequestedSessionIdValid();
//	}
//
//	@Override
//	public boolean isRequestedSessionIdFromCookie() {
//		return getDelegate().isRequestedSessionIdFromCookie();
//	}
//
//	@Override
//	public boolean isRequestedSessionIdFromURL() {
//		return getDelegate().isRequestedSessionIdFromURL();
//	}
//
//	@Override
//	@Deprecated
//	public boolean isRequestedSessionIdFromUrl() {
//		return getDelegate().isRequestedSessionIdFromUrl();
//	}
//}
