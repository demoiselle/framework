/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.bruteforce;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.store.LocalSecurityStore;
import org.demoiselle.jee.security.store.SecurityStore;

/**
 * Proteção contra brute force com lockout por chave (IP ou principal).
 *
 * <p>Mantém contadores de tentativas inválidas e aplica bloqueio temporário
 * após exceder o limite configurado. A partir da versão 4.1 a contagem e o
 * lockout são delegados a um {@link SecurityStore} atômico, garantindo
 * operação correta sob concorrência, expiração automática das tentativas
 * (mesma janela do lockout) e teto global de entradas. A API pública é
 * preservada; a "chave" aceita tanto um IP quanto um identificador de
 * principal resolvido pelo chamador.</p>
 *
 * @author Demoiselle Framework
 */
@ApplicationScoped
public class BruteForceGuard {

    private static final String COUNT_NS = "bruteforce.count";
    private static final String LOCK_NS = "bruteforce.lock";

    @Inject
    private DemoiselleSecurityConfig config;

    @Inject
    private Instance<SecurityStore> storeInstance;

    private SecurityStore store;

    /** CDI constructor. */
    public BruteForceGuard() {
    }

    /**
     * Constructor for direct/non-CDI usage and tests.
     *
     * @param config the security configuration
     * @param store  the backing store
     */
    public BruteForceGuard(DemoiselleSecurityConfig config, SecurityStore store) {
        this.config = config;
        this.store = store;
    }

    @PostConstruct
    void init() {
        if (store == null) {
            if (storeInstance != null && storeInstance.isResolvable()) {
                store = storeInstance.get();
            } else {
                store = new LocalSecurityStore();
            }
        }
    }

    private SecurityStore store() {
        if (store == null) {
            store = new LocalSecurityStore();
        }
        return store;
    }

    private DemoiselleSecurityConfig config() {
        if (config == null) {
            config = new DemoiselleSecurityConfig();
        }
        return config;
    }

    private long windowMillis() {
        return config().getBruteForceLockoutDuration() * 1000L;
    }

    /**
     * Verifica se a chave está bloqueada.
     *
     * @param key IP ou principal do chamador
     * @return segundos restantes de bloqueio, ou -1 se não bloqueado
     */
    public int isBlocked(String key) {
        long remaining = store().lockRemainingMillis(LOCK_NS, key);
        if (remaining <= 0) {
            return -1;
        }
        return (int) (remaining / 1000) + 1;
    }

    /**
     * Registra uma tentativa falha para a chave. Aplica lockout ao atingir
     * {@code bruteForceMaxAttempts}.
     *
     * @param key IP ou principal do chamador
     */
    public void recordFailedAttempt(String key) {
        long count = store().incrementAndGet(COUNT_NS, key, windowMillis());
        if (count >= config().getBruteForceMaxAttempts()) {
            long lockUntil = System.currentTimeMillis()
                    + (config().getBruteForceLockoutDuration() * 1000L);
            store().lock(LOCK_NS, key, lockUntil);
        }
    }

    /**
     * Remove todas as tentativas e o lockout registrados para a chave.
     *
     * @param key IP ou principal do chamador
     */
    public void resetAttempts(String key) {
        store().remove(COUNT_NS, key);
        store().remove(LOCK_NS, key);
    }
}
