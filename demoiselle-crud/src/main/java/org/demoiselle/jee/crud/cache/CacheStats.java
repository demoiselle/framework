/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

/**
 * Immutable snapshot of cache metrics exposed by a {@link CacheBackend}.
 *
 * @param hits      number of lookups that returned a live cached value
 * @param misses    number of lookups that found no live value
 * @param evictions number of entries removed due to the size cap or TTL expiry
 * @param size      current number of live entries
 *
 * @author SERPRO
 */
public record CacheStats(long hits, long misses, long evictions, long size) {

    /**
     * @return the hit ratio in {@code [0,1]}, or {@code 0} when there were no lookups
     */
    public double hitRatio() {
        long total = hits + misses;
        return total == 0 ? 0.0d : (double) hits / (double) total;
    }
}
