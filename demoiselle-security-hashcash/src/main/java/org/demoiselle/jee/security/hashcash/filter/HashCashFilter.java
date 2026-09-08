/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import static jakarta.ws.rs.Priorities.HEADER_DECORATOR;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.security.hashcash.annotation.HashCash;
import org.demoiselle.jee.security.hashcash.execution.Generator;

/**
 * Filtro que aplica o desafio HashCash (proof-of-work) a métodos anotados com
 * {@link HashCash}.
 *
 * <p>No fluxo de resposta, emite um desafio assinado vinculado ao recurso
 * (cabeçalho {@code x-hashcash-challenge}) junto com a versão e a dificuldade.
 * No fluxo de requisição, exige que o cliente reenvie o desafio
 * ({@code x-hashcash-challenge}) e a solução ({@code x-hashcash-result}); a
 * solução é validada contra o recurso, a expiração, a dificuldade e a
 * unicidade (anti-replay). Em caso de falha, responde {@code 402 Payment
 * Required} indicando que um novo proof-of-work é necessário.</p>
 *
 * @author SERPRO
 */
@Provider
@Priority(HEADER_DECORATOR)
public class HashCashFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(HashCashFilter.class.getName());

    static final String CHALLENGE_HEADER = "x-hashcash-challenge";
    static final String RESULT_HEADER = "x-hashcash-result";

    @Context
    private ResourceInfo info;

    @Inject
    private Generator gera;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method == null || classe == null || method.getAnnotation(HashCash.class) == null) {
            return;
        }

        String resource = resourceKey(req);
        String challenge = firstHeader(req, CHALLENGE_HEADER);
        String result = firstHeader(req, RESULT_HEADER);

        boolean ok;
        try {
            ok = challenge != null && result != null
                    && gera.validateHashCash(challenge, result, resource);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "SHA-256 unavailable", e);
            ok = false;
        }

        if (!ok) {
            req.abortWith(Response.status(Response.Status.PAYMENT_REQUIRED).build());
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method != null && classe != null && method.getAnnotation(HashCash.class) != null) {
            res.getHeaders().putSingle(CHALLENGE_HEADER, gera.token(resourceKey(req)));
            res.getHeaders().putSingle("x-hashcash-version", "1");
            res.getHeaders().putSingle("x-hashcash-bits", Integer.toString(gera.difficultyBits()));
        }
    }

    private static String resourceKey(ContainerRequestContext req) {
        if (req.getUriInfo() != null && req.getUriInfo().getPath() != null) {
            return req.getMethod() + ' ' + req.getUriInfo().getPath();
        }
        return req.getMethod();
    }

    private static String firstHeader(ContainerRequestContext req, String name) {
        if (!req.getHeaders().containsKey(name)) {
            return null;
        }
        String value = req.getHeaderString(name);
        return (value == null || value.isBlank()) ? null : value;
    }
}
