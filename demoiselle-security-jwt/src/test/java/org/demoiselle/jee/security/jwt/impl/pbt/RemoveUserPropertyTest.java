/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl.pbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jee-migration-v4, Property 9: removeUser() não lança exceção e limpa o token
 *
 * Validates: Requirements 10.1, 10.2
 *
 * For any DemoiselleUser generated, invoking removeUser(user) on TokenManagerImpl
 * should not throw UnsupportedOperationException, and after invocation the token
 * should be null or empty.
 */
class RemoveUserPropertyTest {

    /**
     * Simple DemoiselleUser implementation for testing purposes.
     */
    static class TestDemoiselleUser implements DemoiselleUser {
        private String identity;
        private String name;
        private final List<String> roles = new ArrayList<>();
        private final Map<String, List<String>> permissions = new ConcurrentHashMap<>();
        private final Map<String, String> params = new ConcurrentHashMap<>();

        TestDemoiselleUser(String identity, String name) {
            this.identity = identity;
            this.name = name;
        }

        @Override public String getIdentity() { return identity; }
        @Override public void setIdentity(String id) { this.identity = id; }
        @Override public String getName() { return name; }
        @Override public void setName(String name) { this.name = name; }
        @Override public void addRole(String role) { if (!roles.contains(role)) roles.add(role); }
        @Override public void removeRole(String role) { roles.remove(role); }
        @Override public List<String> getRoles() { return roles; }
        @Override public Map<String, List<String>> getPermissions() { return permissions; }
        @Override public List<String> getPermissions(String resource) { return permissions.get(resource); }
        @Override public void addPermission(String resource, String operation) {
            permissions.computeIfAbsent(resource, k -> new ArrayList<>()).add(operation);
        }
        @Override public void removePermission(String resource, String operation) {
            List<String> ops = permissions.get(resource);
            if (ops != null) ops.remove(operation);
        }
        @Override public Map<String, String> getParams() { return params; }
        @Override public String getParams(String key) { return params.get(key); }
        @Override public void addParam(String key, String value) { params.put(key, value); }
        @Override public void removeParam(String key) { params.remove(key); }
        @Override public DemoiselleUser clone() {
            TestDemoiselleUser copy = new TestDemoiselleUser(identity, name);
            copy.roles.addAll(roles);
            copy.permissions.putAll(permissions);
            copy.params.putAll(params);
            return copy;
        }
    }

    /**
     * Simple Token implementation for testing.
     */
    static class TestToken implements Token {
        private String key;
        private TokenType type;

        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
    }

    @Provide
    Arbitrary<TestDemoiselleUser> demoiselleUsers() {
        Arbitrary<String> identities = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100);
        Arbitrary<List<String>> roles = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                .list().ofMaxSize(5);

        return Combinators.combine(identities, names, roles).as((id, name, roleList) -> {
            TestDemoiselleUser user = new TestDemoiselleUser(id, name);
            roleList.forEach(user::addRole);
            return user;
        });
    }

    /**
     * P9: For any DemoiselleUser, removeUser() must not throw
     * UnsupportedOperationException and the token key must be null afterwards.
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-9_removeUser-no-exception")
    void removeUserShouldNotThrowAndShouldClearToken(
            @ForAll("demoiselleUsers") TestDemoiselleUser user) throws Exception {

        // Create a TokenManagerImpl via reflection to inject test dependencies
        TokenManagerImpl tokenManager = new TokenManagerImpl();
        TestToken token = new TestToken();
        token.setKey("some-existing-token-value");
        token.setType(TokenType.JWT);

        // Inject the token field via reflection
        java.lang.reflect.Field tokenField = TokenManagerImpl.class.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(tokenManager, token);

        // Inject a TokenBlacklist via reflection
        java.lang.reflect.Field blacklistField = TokenManagerImpl.class.getDeclaredField("tokenBlacklist");
        blacklistField.setAccessible(true);
        blacklistField.set(tokenManager, new org.demoiselle.jee.security.jwt.impl.TokenBlacklist());

        // Inject a JwtTokenValidatorImpl via reflection
        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        java.lang.reflect.Field validatorField = TokenManagerImpl.class.getDeclaredField("jwtTokenValidator");
        validatorField.setAccessible(true);
        validatorField.set(tokenManager, validator);

        // removeUser must not throw UnsupportedOperationException
        assertDoesNotThrow(() -> tokenManager.removeUser(user),
                "removeUser() should not throw for user: " + user.getName());

        // After removeUser, the token key should be null
        assertNull(token.getKey(),
                "Token key should be null after removeUser() for user: " + user.getName());
    }
}
