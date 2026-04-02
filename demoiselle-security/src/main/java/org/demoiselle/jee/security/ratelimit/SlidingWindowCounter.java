package org.demoiselle.jee.security.ratelimit;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Contador de janela deslizante para rate limiting.
 * Mantém registros de invocações por chave (IP) e verifica se o limite foi excedido.
 * Thread-safe via ConcurrentHashMap sem bloqueio global.
 */
@ApplicationScoped
public class SlidingWindowCounter {

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> counters =
        new ConcurrentHashMap<>();

    /**
     * Registra uma invocação e verifica se está dentro do limite.
     *
     * @param key           chave de identificação (ex: IP do cliente)
     * @param maxRequests   número máximo de requisições permitidas na janela
     * @param windowSeconds tamanho da janela em segundos
     * @return segundos restantes até a janela expirar (Retry-After), ou -1 se permitido
     */
    public int recordAndCheck(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        ConcurrentLinkedDeque<Long> timestamps = counters
            .computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Remove registros expirados
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            // Calcula Retry-After baseado no registro mais antigo na janela
            long oldestInWindow = timestamps.peekFirst();
            int retryAfter = (int) ((oldestInWindow + (windowSeconds * 1000L) - now) / 1000) + 1;
            return Math.max(retryAfter, 1);
        }

        timestamps.addLast(now);
        return -1; // permitido
    }
}
