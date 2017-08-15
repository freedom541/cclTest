package com.ccl.jersey.codegen;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DataMap<K,V> {
	int size();

	boolean isEmpty();

	boolean containsKey(K key);

	boolean containsValue(V value);

	V get(K key);

	V put(K key, V value);

	V removeByKey(K key);

	void putAll(Map<K, V> m);

	void clear();

	Set<K> keySet();

	Collection<V> values();

	Set<Map.Entry<K, V>> entrySet();

	Map<K, V> toMap();
	
	void putAll(DataMap<K, V> dataMap);

}
