package com.hiveworkshop.wc3.units.objectdata;

import java.util.Iterator;

import com.etheller.collections.ArrayList;
import com.etheller.collections.CollectionView;
import com.etheller.collections.LinkedHashMap;
import com.etheller.collections.List;
import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.etheller.collections.MapView.Entry;

public final class ChangeMap implements Iterable<MapView.Entry<War3ID, List<Change>>> {
	private final Map<War3ID, List<Change>> idToChanges = new LinkedHashMap<>();

	public void add(final War3ID war3Id, final Change change) {
		List<Change> list = idToChanges.get(war3Id);
		if (list == null) {
			list = new ArrayList<>();
			idToChanges.put(war3Id, list);
		}
		list.add(change);
	}

	public void add(final War3ID war3Id, final List<Change> changes) {
		for (final Change change : changes) {
			add(war3Id, change);
		}
	}

	public List<Change> get(final War3ID war3ID) {
		return idToChanges.get(war3ID);
	}

	public void delete(final War3ID war3ID, final Change change) {
		if (idToChanges.containsKey(war3ID)) {
			final List<Change> changeList = idToChanges.get(war3ID);
			changeList.remove(change);
			if (CollectionView.Util.isEmpty(changeList)) {
				idToChanges.remove(war3ID);
			}
		}
	}

	@Override
	public Iterator<Entry<War3ID, List<Change>>> iterator() {
		return idToChanges.iterator();
	}

	public int size() {
		return idToChanges.size();
	}

	public void clear() {
		idToChanges.clear();
	}
}
