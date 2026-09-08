package org.demoiselle.jee.security.ratelimit;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.demoiselle.jee.security.store.LocalSecurityStore;
import org.demoiselle.jee.security.store.SecurityStore;

/**
 * Contador de janela deslizante para rate limiting.
 *
 * <p>Mantém registros de invocações por chave e verifica se o limite foi
 * excedido. A partir da versão 4.1 delega a contagem para um
 * {@link SecurityStore} atômico, o que garante operação segura sob concorrência
 * e adiciona expiração automática (TTL da janela) e teto global de entradas.
 * A API pública ({@link #recordAndCheck(String, int, int)}) é preservada.</p>
 */
@ApplicationScoped
public class SlidingWindowCounter {

    private static final String NAMESPACE = "ratelimit";

    @Inject
    private Instance<SecurityStore> storeInstance;

    private SecurityStore store;

    /** CDI constructor. */
    public SlidingWindowCounter() {
    }

    /**
     * Constructor for direct/non-CDI usage and tests.
     *
     * @param store the backing store
     */
    public SlidingWindowCounter(SecurityStore store) {
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

    /**
     * Registra uma invocação e verifica se está dentro do limite.
     *
     * @param key           chave de identificação (ex: IP do cliente ou principal)
     * @param maxRequests   número máximo de requisições permitidas na janela
     * @param windowSeconds tamanho da janela em segundos
     * @return segundos restantes até a janela expirar (Retry-After), ou -1 se permitido
     */
    public int recordAndCheck(String key, int maxRequests, int windowSeconds) {
        final long windowMillis = windowSeconds * 1000L;
        final SecurityStore s = store();

        long count = s.incrementAndGet(NAMESPACE, key, windowMillis);
        if (count > maxRequests) {
            long now = System.currentTimeMillis();
            long windowStart = s.windowStartMillis(NAMESPACE, key);
            int retryAfter;
            if (windowStart < 0) {
                retryAfter = windowSeconds;
            } else {
                retryAfter = (int) ((windowStart + windowMillis - now) / 1000) + 1;
            }
            return Math.min(Math.max(retryAfter, 1), windowSeconds);
        }
        return -1; // permitido
    }
}
