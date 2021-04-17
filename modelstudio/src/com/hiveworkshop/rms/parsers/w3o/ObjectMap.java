package com.hiveworkshop.rms.parsers.w3o;

import com.hiveworkshop.rms.util.War3ID;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ObjectMap implements Iterable<Map.Entry<War3ID, ObjectDataChangeEntry>> {
	private final Map<War3ID, ObjectDataChangeEntry> idToDataChangeEntry;
	private final Set<War3ID> lowerCaseKeySet;

	public ObjectMap() {
		idToDataChangeEntry = new HashMap<>();
		lowerCaseKeySet = new HashSet<>();
	}

	public void clear() {
		idToDataChangeEntry.clear();
		lowerCaseKeySet.clear();
	}

	public ObjectDataChangeEntry remove(final War3ID key) {
		lowerCaseKeySet.remove(War3ID.fromString(key.toString().toLowerCase()));
		return idToDataChangeEntry.remove(key);
	}

	public Set<War3ID> keySet() {
		return idToDataChangeEntry.keySet();
	}

	public ObjectDataChangeEntry put(final War3ID key, final ObjectDataChangeEntry value) {
		lowerCaseKeySet.add(War3ID.fromString(key.toString().toLowerCase()));
		return idToDataChangeEntry.put(key, value);
	}

	public Set<Map.Entry<War3ID, ObjectDataChangeEntry>> entrySet() {
		return idToDataChangeEntry.entrySet();
	}

	public ObjectDataChangeEntry get(final War3ID key) {
		return idToDataChangeEntry.get(key);
	}

	public boolean containsKey(final War3ID key) {
		return idToDataChangeEntry.containsKey(key);
	}

	public boolean containsKeyCaseInsensitive(final War3ID key) {
		return lowerCaseKeySet.contains(War3ID.fromString(key.toString().toLowerCase()));
	}

	public boolean containsValue(final ObjectDataChangeEntry value) {
		return idToDataChangeEntry.containsValue(value);
	}

	public Collection<ObjectDataChangeEntry> values() {
		return idToDataChangeEntry.values();
	}

	public int size() {
		return idToDataChangeEntry.size();
	}

	@Override
	public Iterator<Map.Entry<War3ID, ObjectDataChangeEntry>> iterator() {
		return idToDataChangeEntry.entrySet().iterator();
	}

	@Override
	public ObjectMap clone() {
		final ObjectMap clone = new ObjectMap();

		for (Map.Entry<War3ID, ObjectDataChangeEntry> entry : idToDataChangeEntry.entrySet()) {
			clone.put(entry.getKey(), entry.getValue());
		}

		return clone;
	}
}
