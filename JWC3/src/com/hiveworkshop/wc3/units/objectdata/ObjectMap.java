package com.hiveworkshop.wc3.units.objectdata;

import java.util.Iterator;

import com.etheller.collections.CollectionView;
import com.etheller.collections.LinkedHashMap;
import com.etheller.collections.Map;
import com.etheller.collections.MapView.Entry;
import com.etheller.collections.MapView.ForEach;
import com.etheller.collections.SetView;

public final class ObjectMap implements Iterable<Entry<War3ID, ObjectDataChangeEntry>> {
	private final Map<War3ID, ObjectDataChangeEntry> idToDataChangeEntry;

	public ObjectMap() {
		idToDataChangeEntry = new LinkedHashMap<>();
	}

	public void clear() {
		idToDataChangeEntry.clear();
	}

	public ObjectDataChangeEntry remove(final War3ID key) {
		return idToDataChangeEntry.remove(key);
	}

	public SetView<War3ID> keySet() {
		return idToDataChangeEntry.keySet();
	}

	public ObjectDataChangeEntry put(final War3ID key, final ObjectDataChangeEntry value) {
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
