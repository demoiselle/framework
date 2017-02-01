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

import javax.enterprise.context.ApplicationScoped;

/**
 * Dynamic Manage Cache - Responsible for Script Cache
 *
 * @author SERPRO
 */
@ApplicationScoped
public class DynamicManagerCache implements Serializable {

    private static final long serialVersionUID = 2305168056315491913L;

    static Map<String, ConcurrentHashMap<String, Object>> scriptCache = new ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>();
    static Map<String, Object> engineList = new ConcurrentHashMap<String, Object>();
}
