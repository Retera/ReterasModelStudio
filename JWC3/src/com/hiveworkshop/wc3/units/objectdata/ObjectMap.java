package com.hiveworkshop.wc3.units.objectdata;

import java.util.Iterator;

import com.etheller.collections.CollectionView;
import com.etheller.collections.HashSet;
import com.etheller.collections.LinkedHashMap;
import com.etheller.collections.Map;
import com.etheller.collections.MapView.Entry;
import com.etheller.collections.MapView.ForEach;
import com.etheller.collections.Set;
import com.etheller.collections.SetView;

public final class ObjectMap implements Iterable<Entry<War3ID, ObjectDataChangeEntry>> {
	private final Map<War3ID, ObjectDataChangeEntry> idToDataChangeEntry;
	private final Set<War3ID> lowerCaseKeySet;

	public ObjectMap() {
		idToDataChangeEntry = new LinkedHashMap<>();
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

	public SetView<War3ID> keySet() {
		return idToDataChangeEntry.keySet();
	}

	public ObjectDataChangeEntry put(final War3ID key, final ObjectDataChangeEntry value) {
		lowerCaseKeySet.add(War3ID.fromString(key.toString().toLowerCase()));
		return idToDataChangeEntry.put(key, value);
	}

	public SetView<Entry<War3ID, ObjectDataChangeEntry>> entrySet() {
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

	public CollectionView<ObjectDataChangeEntry> values() {
		return idToDataChangeEntry.values();
	}

	public int size() {
		return idToDataChangeEntry.size();
	}

	public void forEach(final ForEach<? super War3ID, ? super ObjectDataChangeEntry> forEach) {
		idToDataChangeEntry.forEach(forEach);
	}

	@Override
	public Iterator<Entry<War3ID, ObjectDataChangeEntry>> iterator() {
		return idToDataChangeEntry.iterator();
	}

	@Override
	public ObjectMap clone() {
		final ObjectMap clone = new ObjectMap();
		forEach(new ForEach<War3ID, ObjectDataChangeEntry>() {
			@Override
			public boolean onEntry(final War3ID key, final ObjectDataChangeEntry value) {
				clone.put(key, value);
				return true;
			}
		});
		return clone;
	}
}
