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
