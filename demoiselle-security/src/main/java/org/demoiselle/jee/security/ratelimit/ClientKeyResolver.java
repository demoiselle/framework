/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.ratelimit;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;

/**
 * Resolves the throttling/lockout key for the current request.
 *
 * <p>Resolution order:</p>
 * <ol>
 *   <li>the authenticated principal identifier, when a {@link SecurityContext}
 *       with a logged-in user is available — prefixed with {@code "user:"} so
 *       an authenticated user is throttled independently of their IP;</li>
 *   <li>otherwise the client IP, prefixed with {@code "ip:"}.</li>
 * </ol>
 *
 * <p><strong>Proxy safety.</strong> Forwarded headers ({@code X-Forwarded-For},
 * {@code Forwarded}) are <em>not</em> trusted by default, because a client can
 * spoof them to evade or amplify rate limiting. The client IP is taken from
 * {@link HttpServletRequest#getRemoteAddr()}. Forwarded headers are only honoured
 * when the immediate peer ({@code getRemoteAddr()}) is present in the configured
 * allow-list of trusted proxies
 * ({@code demoiselle.security.trustedProxies}); in that case the chain is read
 * from the nearest hop and the first address that is not trusted is used.</p>
 *
 * @author SERPRO
 */
public final class ClientKeyResolver {

    private static final String XFF = "X-Forwarded-For";

    private ClientKeyResolver() {
    }

    /**
     * Resolves the key for the given request/principal.
     *
     * @param request         the current HTTP request (may be {@code null})
     * @param securityContext the security context (may be {@code null})
     * @param config          the security configuration (may be {@code null})
     * @return a stable, non-null key
     */
    public static String resolve(HttpServletRequest request,
                                 SecurityContext securityContext,
                                 DemoiselleSecurityConfig config) {
        String principal = principalKey(securityContext);
        if (principal != null) {
            return "user:" + principal;
        }
        return "ip:" + resolveClientIp(request, config);
    }

    /**
     * Returns the authenticated principal identifier, or {@code null} when no
     * user is logged in or the context is unavailable.
     *
     * @param securityContext the security context (may be {@code null})
     * @return the principal identifier or {@code null}
     */
    public static String principalKey(SecurityContext securityContext) {
        if (securityContext == null) {
            return null;
        }
        try {
            if (!securityContext.isLoggedIn()) {
                return null;
            }
            DemoiselleUser user = securityContext.getUser();
            if (user == null) {
                return null;
            }
            String id = user.getIdentity();
            if (id != null && !id.isBlank()) {
                return id;
            }
            String name = user.getName();
            return (name != null && !name.isBlank()) ? name : null;
        } catch (RuntimeException e) {
            // A misbehaving token manager must never break throttling.
            return null;
        }
    }

    /**
     * Resolves the effective client IP, honouring trusted proxies only.
     *
     * @param request the current HTTP request (may be {@code null})
     * @param config  the security configuration (may be {@code null})
     * @return the client IP, or {@code "unknown"} when it cannot be determined
     */
    public static String resolveClientIp(HttpServletRequest request,
                                          DemoiselleSecurityConfig config) {
        if (request == null) {
            return "unknown";
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            remoteAddr = "unknown";
        }

        List<String> trusted = (config == null) ? List.of() : config.getTrustedProxies();
        if (trusted.isEmpty() || !trusted.contains(remoteAddr)) {
            // Default and safe: ignore forwarded headers entirely.
            return remoteAddr;
        }

        String forwarded = request.getHeader(XFF);
        String clientFromXff = nearestUntrusted(forwarded, trusted);
        return (clientFromXff != null) ? clientFromXff : remoteAddr;
    }

    /**
     * Parses {@code X-Forwarded-For} and returns the right-most address that is
     * not a trusted proxy — i.e. the closest hop we cannot vouch for, which is
     * the real client as far as our trusted chain can attest.
     */
    private static String nearestUntrusted(String xff, List<String> trusted) {
        if (xff == null || xff.isBlank()) {
            return null;
        }
        String[] hops = xff.split(",");
        // Walk from the right (nearest to us) towards the client, skipping our
        // trusted proxies; the first non-trusted hop is the attributable client.
        for (int i = hops.length - 1; i >= 0; i--) {
            String hop = hops[i].trim();
            if (hop.isEmpty()) {
                continue;
            }
            if (!trusted.contains(hop)) {
                return hop;
            }
        }
        return null;
    }
}
