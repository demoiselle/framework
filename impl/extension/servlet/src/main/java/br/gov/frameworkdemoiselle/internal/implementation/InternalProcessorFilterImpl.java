package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import br.gov.frameworkdemoiselle.annotation.StaticScoped;
import br.gov.frameworkdemoiselle.util.ServletFilter.InternalProcessorFilter;

@StaticScoped
public class InternalProcessorFilterImpl implements InternalProcessorFilter {

	private List<Filter> filters;

	public InternalProcessorFilterImpl() {
		filters = new ArrayList<Filter>();

		filters.add(new HttpServletRequestProducerFilter());
		filters.add(new HttpServletResponseProducerFilter());
		
		// TODO Analizar o uso do BasicAuthenticationFilter
		// filters.add(new BasicAuthenticationFilter());
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		for (Filter filter : filters) {
			filter.init(config);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		FilterChain emptyChain = createEmptyChain();

		for (Filter filter : filters) {
			filter.doFilter(request, response, emptyChain);
		}
	}

	@Override
	public void destroy() {
		for (Filter filter : filters) {
			filter.destroy();
		}
	}

	private FilterChain createEmptyChain() {
		return new FilterChain() {

			@Override
			public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			}
		};
	}
}
