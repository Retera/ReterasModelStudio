package com.hiveworkshop.wc3.units.objectdata;

import com.etheller.collections.List;
import com.etheller.collections.MapView;

public final class ObjectDataChangeEntry {
	private War3ID oldId;
	private War3ID newId;
	private final ChangeMap changes;

	public ObjectDataChangeEntry(final War3ID oldId, final War3ID newId) {
		this.oldId = oldId;
		this.newId = newId;
		this.changes = new ChangeMap();
	}

	@Override
	public ObjectDataChangeEntry clone() {
		final ObjectDataChangeEntry objectDataChangeEntry = new ObjectDataChangeEntry(oldId, newId);
		for (final MapView.Entry<War3ID, List<Change>> entry : changes) {
			objectDataChangeEntry.getChanges().add(entry.getKey(), entry.getValue());
		}
		return objectDataChangeEntry;
	}

	public ChangeMap getChanges() {
		return changes;
	}

	public War3ID getOldId() {
		return oldId;
	}

	public void setOldId(final War3ID oldId) {
		this.oldId = oldId;
	}

	public War3ID getNewId() {
		return newId;
	}

	public void setNewId(final War3ID newId) {
		this.newId = newId;
	}
}
