/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
/**
 * Filtro que habilita o CORS no sistema inteiro. Para ativar é necessário adicionar o seguinte trecho de XML dentro do web.xml:
 * 
 * <pre>
 * {@code
 * 	<filter>
 * 		<filter-name>CorsFilter</filter-name>
 * 		<filter-class>org.demoiselle.jee.rest.filter.JaxRsFilter</filter-class>
 * 	</filter>
 * 	<filter-mapping>
 * 		<filter-name>CorsFilter</filter-name>
 * 		<url-pattern>/*</url-pattern>
 * 	</filter-mapping>
 * }
 * </pre>
 * @author SERPRO
 *
 */
public class JaxRsFilter implements Filter {

	@Inject
	private Logger LOG;

	@Context
	private ResourceInfo info;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpResp = (HttpServletResponse) response;

		httpResp.setHeader("Demoiselle", "3.0.0");

		if (isPreflightRequest(httpReq)) {
			httpResp.setHeader("Access-Control-Allow-Origin", httpReq.getHeader("Origin")); //$NON-NLS-1$ //$NON-NLS-2$
			httpResp.setHeader("Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			httpResp.setHeader("Access-Control-Max-Age", "1800"); //$NON-NLS-1$ //$NON-NLS-2$
			httpResp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,HEAD,DELETE"); //$NON-NLS-1$ //$NON-NLS-2$
			httpResp.setHeader("Access-Control-Allow-Headers", //$NON-NLS-1$
					"X-Requested-With,Content-Type,Accept,Origin,Authorization"); //$NON-NLS-1$
			httpResp.setHeader("Access-Control-Expose-Headers", "X-Apiman-Error"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			if (hasOriginHeader(httpReq)) {
				httpResp.setHeader("Access-Control-Allow-Origin", httpReq.getHeader("Origin")); //$NON-NLS-1$ //$NON-NLS-2$
				httpResp.setHeader("Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				httpResp.setHeader("Access-Control-Expose-Headers", "X-Apiman-Error"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			chain.doFilter(httpReq, httpResp);
		}
	}

	@PostConstruct
	public void init() {
		LOG.info("Demoiselle Module - Rest");
	}

	private boolean isPreflightRequest(HttpServletRequest httpReq) {
		return isOptionsMethod(httpReq) && hasOriginHeader(httpReq);
	}

	private boolean isOptionsMethod(HttpServletRequest httpReq) {
		return "OPTIONS".equals(httpReq.getMethod()); //$NON-NLS-1$
	}

	private boolean hasOriginHeader(HttpServletRequest httpReq) {
		String origin = httpReq.getHeader("Origin"); //$NON-NLS-1$
		return origin != null && origin.trim().length() > 0;
	}

	@Override
	public void destroy() {
		
	}

}
