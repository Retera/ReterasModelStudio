package com.hiveworkshop.rms.parsers.w3o;

import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ChangeMap implements Iterable<Map.Entry<War3ID, List<Change>>> {
	private final Map<War3ID, List<Change>> idToChanges = new HashMap<>();

	public void add(final War3ID war3Id, final Change change) {
        List<Change> list = idToChanges.computeIfAbsent(war3Id, k -> new ArrayList<>());
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
			if (changeList.size() == 0) {
				idToChanges.remove(war3ID);
			}
		}
	}

	@Override
	public Iterator<Map.Entry<War3ID, List<Change>>> iterator() {
		return idToChanges.entrySet().iterator();
	}

	public int size() {
		return idToChanges.size();
	}

	public void clear() {
		idToChanges.clear();
	}
}
