package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import br.gov.frameworkdemoiselle.internal.producer.HttpServletRequestProducer;
import br.gov.frameworkdemoiselle.util.Beans;

public class HttpServletRequestProducerFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		Beans.getReference(HttpServletRequestProducer.class).setDelegate((HttpServletRequest) request);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
