package com.hiveworkshop.rms.parsers.w3o;

import com.hiveworkshop.rms.util.War3ID;

import java.util.List;
import java.util.Map;

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
		for (final Map.Entry<War3ID, List<Change>> entry : changes) {
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
