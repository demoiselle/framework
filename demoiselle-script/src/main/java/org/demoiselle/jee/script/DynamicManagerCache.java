/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

/** 
 * Dynamic Manage Cache - Responsible for Script Cache
 *
 * @author SERPRO
 */
@ApplicationScoped 
final class DynamicManagerCache implements Serializable {
	private static final long serialVersionUID = 2305168056315491913L;
	
	static HashMap<String, ConcurrentHashMap<String, Object> > scriptCache = new HashMap <String, ConcurrentHashMap<String, Object> >();	
	static HashMap<String, Object> engineList = new HashMap<String,Object>();	
}
