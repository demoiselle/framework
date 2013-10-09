package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class JDBCConfigurationStore implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, String> properties = new HashMap<String, String>();

	public int size() {
		return properties.size();
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public String get(Object key) {
		return properties.get(key);
	}

	public boolean equals(Object o) {
		return properties.equals(o);
	}

	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	public String put(String key, String value) {
		return properties.put(key, value);
	}

	public int hashCode() {
		return properties.hashCode();
	}

	public String toString() {
		return properties.toString();
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		properties.putAll(m);
	}

	public String remove(Object key) {
		return properties.remove(key);
	}

	public void clear() {
		properties.clear();
	}

	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	public Object clone() {
		return properties.clone();
	}

	public Set<String> keySet() {
		return properties.keySet();
	}

	public Collection<String> values() {
		return properties.values();
	}

	public Set<Entry<String, String>> entrySet() {
		return properties.entrySet();
	}
}
