package com.hiveworkshop.rms.util;

import java.util.*;

public class BiMap<K, V> implements Map<K, V> {
	private Map<K, V> keyToValueMap = new HashMap<>();
	private Map<V, K> valueToKeyMap = new HashMap<>();

	public BiMap() {
	}

	public BiMap(Map<? extends K, ? extends V> m) {
		putAll(m);
	}

	public BiMap(Collection<? extends K> keys, Collection<? extends V> values) {
		if (keys.size() == values.size()) {
			keyToValueMap = new HashMap<>();
			valueToKeyMap = new HashMap<>();

			Iterator<? extends V> vI = values.iterator();
			for (Iterator<? extends K> kI = keys.iterator(); kI.hasNext() && vI.hasNext(); ) {
				put(kI.next(), vI.next());
			}
		}
	}

	public static void main(String[] args) {
		Map<Integer, Integer> ugg = new HashMap<>();
		ugg.put(1, 1);
		System.out.println(ugg.remove(null));
		System.out.println("Woop!");
	}

	@Override
	public int size() {
		return keyToValueMap.size();
	}

	@Override
	public boolean isEmpty() {
		return keyToValueMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return keyToValueMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return valueToKeyMap.containsKey(value);
	}

	@Override
	public V get(Object key) {
		return keyToValueMap.get(key);
	}

	public K getByValue(Object value) {
		return valueToKeyMap.get(value);
	}

	public V getByKey(Object key) {
		return keyToValueMap.get(key);
	}

	@Override
	public V put(K key, V value) {
		valueToKeyMap.put(value, key);
		return keyToValueMap.put(key, value);
	}

	@Override
	public V remove(Object key) {
		V value = keyToValueMap.remove(key);
		if (value != null) {
			valueToKeyMap.remove(value);
		}
		return value;
	}

	public V removeByKey(Object key) {
		V value = keyToValueMap.remove(key);
		valueToKeyMap.remove(value);
		return value;
	}

	public K removeByValue(Object value) {
		K key = valueToKeyMap.remove(value);
		keyToValueMap.remove(key);
		return key;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		keyToValueMap.putAll(m);
		for (K k : m.keySet()) {
			valueToKeyMap.put(m.get(k), k);
		}
	}

	@Override
	public void clear() {
		keyToValueMap.clear();
		valueToKeyMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return keyToValueMap.keySet();
	}

	public Set<V> valueSet() {
		return valueToKeyMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return keyToValueMap.values();
	}

	public Collection<K> keys() {
		return valueToKeyMap.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return keyToValueMap.entrySet();
	}
}
