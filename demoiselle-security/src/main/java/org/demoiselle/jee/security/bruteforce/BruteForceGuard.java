/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.bruteforce;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;

/**
 * Proteção contra brute force com lockout por IP.
 * Mantém contadores de tentativas inválidas por IP e aplica bloqueio
 * temporário após exceder o limite configurado.
 *
 * @author Demoiselle Framework
 */
@ApplicationScoped
public class BruteForceGuard {

    private record AttemptInfo(int count, long lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    @Inject
    private DemoiselleSecurityConfig config;

    /**
     * Verifica se o IP está bloqueado.
     * @param ip endereço IP do chamador
     * @return segundos restantes de bloqueio, ou -1 se não bloqueado
     */
    public int isBlocked(String ip) {
        AttemptInfo info = attempts.get(ip);
        if (info == null) return -1;

        long now = System.currentTimeMillis();
        if (info.lockedUntil > 0 && now < info.lockedUntil) {
            return (int) ((info.lockedUntil - now) / 1000) + 1;
        }

        // Lockout expirou — reseta
        if (info.lockedUntil > 0 && now >= info.lockedUntil) {
            attempts.remove(ip);
            return -1;
        }

        return -1;
    }

    /**
     * Registra uma tentativa falha para o IP.
     * Aplica lockout ao atingir maxAttempts.
     * @param ip endereço IP do chamador
     */
    public void recordFailedAttempt(String ip) {
        attempts.compute(ip, (key, existing) -> {
            int newCount = (existing == null) ? 1 : existing.count + 1;
            if (newCount >= config.getBruteForceMaxAttempts()) {
                long lockUntil = System.currentTimeMillis()
                    + (config.getBruteForceLockoutDuration() * 1000L);
                return new AttemptInfo(newCount, lockUntil);
            }
            return new AttemptInfo(newCount, 0);
        });
    }

    /**
     * Remove todas as tentativas registradas para o IP.
     * @param ip endereço IP do chamador
     */
    public void resetAttempts(String ip) {
        attempts.remove(ip);
    }
}
