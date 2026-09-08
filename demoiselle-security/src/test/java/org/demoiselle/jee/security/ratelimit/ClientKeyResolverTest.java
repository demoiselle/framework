/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.ratelimit;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientKeyResolverTest {

    // --- Proxy safety ---

    @Test
    void ignoresForwardedHeaderByDefault() {
        HttpServletRequest req = request("203.0.113.9", Map.of("X-Forwarded-For", "1.2.3.4"));
        DemoiselleSecurityConfig cfg = new DemoiselleSecurityConfig();

        // no trusted proxies configured → forwarded header ignored
        assertEquals("ip:203.0.113.9", ClientKeyResolver.resolve(req, null, cfg));
    }

    @Test
    void honoursForwardedHeaderOnlyFromTrustedProxy() throws Exception {
        HttpServletRequest req = request("10.0.0.1", Map.of("X-Forwarded-For", "1.2.3.4"));
        DemoiselleSecurityConfig cfg = withTrustedProxies("10.0.0.1");

        assertEquals("1.2.3.4", ClientKeyResolver.resolveClientIp(req, cfg));
    }

    @Test
    void spoofedForwardedHeaderFromUntrustedPeerIsIgnored() throws Exception {
        HttpServletRequest req = request("198.51.100.7", Map.of("X-Forwarded-For", "1.2.3.4"));
        DemoiselleSecurityConfig cfg = withTrustedProxies("10.0.0.1");

        assertEquals("198.51.100.7", ClientKeyResolver.resolveClientIp(req, cfg));
    }

    @Test
    void picksClosestNonTrustedHopInChain() throws Exception {
        HttpServletRequest req = request("10.0.0.1",
                Map.of("X-Forwarded-For", "1.2.3.4, 10.0.0.2, 10.0.0.1"));
        DemoiselleSecurityConfig cfg = withTrustedProxies("10.0.0.1", "10.0.0.2");

        assertEquals("1.2.3.4", ClientKeyResolver.resolveClientIp(req, cfg));
    }

    @Test
    void fallsBackToRemoteAddrWhenForwardedIsAllTrusted() throws Exception {
        HttpServletRequest req = request("10.0.0.1",
                Map.of("X-Forwarded-For", "10.0.0.2, 10.0.0.1"));
        DemoiselleSecurityConfig cfg = withTrustedProxies("10.0.0.1", "10.0.0.2");

        assertEquals("10.0.0.1", ClientKeyResolver.resolveClientIp(req, cfg));
    }

    @Test
    void nullRequestYieldsUnknown() {
        assertEquals("unknown", ClientKeyResolver.resolveClientIp(null, null));
        assertEquals("ip:unknown", ClientKeyResolver.resolve(null, null, null));
    }

    // --- Per-principal keying ---

    @Test
    void prefersAuthenticatedPrincipal() {
        HttpServletRequest req = request("203.0.113.9", Map.of());
        DemoiselleUser user = new DemoiselleUserImpl();
        user.setIdentity("alice");
        SecurityContext ctx = new StubSecurityContext(true, user);

        assertEquals("user:alice", ClientKeyResolver.resolve(req, ctx, new DemoiselleSecurityConfig()));
    }

    @Test
    void fallsBackToIpWhenNotLoggedIn() {
        HttpServletRequest req = request("203.0.113.9", Map.of());
        SecurityContext ctx = new StubSecurityContext(false, null);

        assertEquals("ip:203.0.113.9", ClientKeyResolver.resolve(req, ctx, new DemoiselleSecurityConfig()));
    }

    @Test
    void misbehavingContextFallsBackToIp() {
        HttpServletRequest req = request("203.0.113.9", Map.of());
        SecurityContext ctx = new StubSecurityContext(true, null) {
            @Override public boolean isLoggedIn() { throw new IllegalStateException("boom"); }
        };
        assertEquals("ip:203.0.113.9", ClientKeyResolver.resolve(req, ctx, new DemoiselleSecurityConfig()));
    }

    // --- helpers ---

    private static DemoiselleSecurityConfig withTrustedProxies(String... proxies) throws Exception {
        DemoiselleSecurityConfig cfg = new DemoiselleSecurityConfig();
        Field f = DemoiselleSecurityConfig.class.getDeclaredField("trustedProxies");
        f.setAccessible(true);
        f.set(cfg, proxies);
        return cfg;
    }

    /** Minimal HttpServletRequest via dynamic proxy (only getRemoteAddr/getHeader matter). */
    private static HttpServletRequest request(String remoteAddr, Map<String, String> headers) {
        Map<String, String> h = new HashMap<>(headers);
        return (HttpServletRequest) Proxy.newProxyInstance(
                ClientKeyResolverTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRemoteAddr" -> remoteAddr;
                    case "getHeader" -> h.get((String) args[0]);
                    default -> defaultReturn(method.getReturnType());
                });
    }

    private static Object defaultReturn(Class<?> rt) {
        if (rt == boolean.class) return false;
        if (rt == int.class) return 0;
        if (rt == long.class) return 0L;
        return null;
    }

    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;
        private final DemoiselleUser user;

        StubSecurityContext(boolean loggedIn, DemoiselleUser user) {
            this.loggedIn = loggedIn;
            this.user = user;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public DemoiselleUser getUser() { return user; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
        @Override public boolean hasRole(String role) { return false; }
        @Override public void setUser(DemoiselleUser loggedUser) { }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return user; }
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) { }
        @Override public void removeUser(DemoiselleUser loggedUser) { }
        @Override public boolean hasAnyRole(String... roles) { return false; }
        @Override public boolean hasAllRoles(String... roles) { return false; }
    }
}
