package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.internal.producer.HttpServletResponseProducer;
import br.gov.frameworkdemoiselle.util.Beans;

public class HttpServletResponseProducerFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		Beans.getReference(HttpServletResponseProducer.class).setDelegate((HttpServletResponse) response);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
