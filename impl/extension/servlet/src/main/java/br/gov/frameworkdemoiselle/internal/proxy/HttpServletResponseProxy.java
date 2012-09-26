//package br.gov.frameworkdemoiselle.internal.proxy;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.Serializable;
//import java.util.Locale;
//
//import javax.enterprise.context.RequestScoped;
//import javax.enterprise.inject.Default;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletResponse;
//
//@Default
//@RequestScoped
//public class HttpServletResponseProxy implements HttpServletResponse, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private transient HttpServletResponse delegate;
//
//	public HttpServletResponseProxy(HttpServletResponse delegate) {
//		this.delegate = delegate;
//	}
//
//	private HttpServletResponse getDelegate() {
//		return delegate;
//	}
//
//	@Override
//	public String getCharacterEncoding() {
//		return getDelegate().getCharacterEncoding();
//	}
//
//	@Override
//	public String getContentType() {
//		return getDelegate().getContentType();
//	}
//
//	@Override
//	public ServletOutputStream getOutputStream() throws IOException {
//		return getDelegate().getOutputStream();
//	}
//
//	@Override
//	public PrintWriter getWriter() throws IOException {
//		return getDelegate().getWriter();
//	}
//
//	@Override
//	public void setCharacterEncoding(String charset) {
//		getDelegate().setCharacterEncoding(charset);
//	}
//
//	@Override
//	public void setContentLength(int len) {
//		getDelegate().setContentLength(len);
//	}
//
//	@Override
//	public void setContentType(String type) {
//		getDelegate().setContentType(type);
//	}
//
//	@Override
//	public void setBufferSize(int size) {
//		getDelegate().setBufferSize(size);
//	}
//
//	@Override
//	public int getBufferSize() {
//		return getDelegate().getBufferSize();
//	}
//
//	@Override
//	public void flushBuffer() throws IOException {
//		getDelegate().flushBuffer();
//	}
//
//	@Override
//	public void resetBuffer() {
//		getDelegate().resetBuffer();
//	}
//
//	@Override
//	public boolean isCommitted() {
//		return getDelegate().isCommitted();
//	}
//
//	@Override
//	public void reset() {
//		getDelegate().reset();
//	}
//
//	@Override
//	public void setLocale(Locale loc) {
//		getDelegate().setLocale(loc);
//	}
//
//	@Override
//	public Locale getLocale() {
//		return getDelegate().getLocale();
//	}
//
//	@Override
//	public void addCookie(Cookie cookie) {
//		getDelegate().addCookie(cookie);
//	}
//
//	@Override
//	public boolean containsHeader(String name) {
//		return getDelegate().containsHeader(name);
//	}
//
//	@Override
//	public String encodeURL(String url) {
//		return getDelegate().encodeURL(url);
//	}
//
//	@Override
//	public String encodeRedirectURL(String url) {
//		return getDelegate().encodeRedirectURL(url);
//	}
//
//	@Override
//	@Deprecated
//	public String encodeUrl(String url) {
//		return getDelegate().encodeUrl(url);
//	}
//
//	@Override
//	@Deprecated
//	public String encodeRedirectUrl(String url) {
//		return getDelegate().encodeRedirectUrl(url);
//	}
//
//	@Override
//	public void sendError(int sc, String msg) throws IOException {
//		getDelegate().sendError(sc, msg);
//	}
//
//	@Override
//	public void sendError(int sc) throws IOException {
//		getDelegate().sendError(sc);
//	}
//
//	@Override
//	public void sendRedirect(String location) throws IOException {
//		getDelegate().sendRedirect(location);
//	}
//
//	@Override
//	public void setDateHeader(String name, long date) {
//		getDelegate().setDateHeader(name, date);
//	}
//
//	@Override
//	public void addDateHeader(String name, long date) {
//		getDelegate().addDateHeader(name, date);
//	}
//
//	@Override
//	public void setHeader(String name, String value) {
//		getDelegate().setHeader(name, value);
//	}
//
//	@Override
//	public void addHeader(String name, String value) {
//		getDelegate().addHeader(name, value);
//	}
//
//	@Override
//	public void setIntHeader(String name, int value) {
//		getDelegate().setIntHeader(name, value);
//	}
//
//	@Override
//	public void addIntHeader(String name, int value) {
//		getDelegate().addIntHeader(name, value);
//	}
//
//	@Override
//	public void setStatus(int sc) {
//		getDelegate().setStatus(sc);
//	}
//
//	@Override
//	@Deprecated
//	public void setStatus(int sc, String sm) {
//		getDelegate().setStatus(sc, sm);
//	}
//}
