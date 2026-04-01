/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Dynamic Manage Cache - Responsible for Script Cache
 *
 * @author SERPRO
 */
@ApplicationScoped
public class DynamicManagerCache implements Serializable {

    private static final long serialVersionUID = 2305168056315491913L;

    private final Map<String, ConcurrentHashMap<String, Object>> scriptCache = new ConcurrentHashMap<>();
    private final Map<String, Object> engineList = new ConcurrentHashMap<>();

    public Map<String, ConcurrentHashMap<String, Object>> getScriptCache() {
        return scriptCache;
    }

    public Map<String, Object> getEngineList() {
        return engineList;
    }
}
