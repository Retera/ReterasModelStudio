package com.hiveworkshop.rms.parsers.slk;

import java.util.*;

public class WarcraftData implements ObjectData {
	List<DataTable> tables = new ArrayList<>();
	Map<StringKey, DataTable> tableMap = new HashMap<>();
	Map<StringKey, WarcraftObject> units = new HashMap<>();

	public void add(final DataTable data, final String name, final boolean canMake) {
		tableMap.put(new StringKey(name), data);
		tables.add(data);
		if (canMake) {
			for (final String id : data.keySet()) {
				if (!units.containsKey(new StringKey(id))) {
					units.put(new StringKey(id), new WarcraftObject(data.get(id).getId(), this));
				}
			}
		}
	}

	public WarcraftData() {
	}

	public List<DataTable> getTables() {
		return tables;
	}

	public void setTables(final List<DataTable> tables) {
		this.tables = tables;
	}

	public DataTable getTable(final String tableName) {
		return tableMap.get(new StringKey(tableName));
	}

	@Override
	public GameObject get(final String id) {
		return units.get(new StringKey(id));
	}

	@Override
	public void setValue(final String id, final String field, final String value) {
		get(id).setField(field, value);
	}

	@Override
	public Set<String> keySet() {
		final Set<String> keySet = new HashSet<>();
		for (final StringKey key : units.keySet()) {
			keySet.add(key.getString());
		}
		return keySet;
	}

	public void cloneUnit(final String parentId, final String cloneId) {
		for (final DataTable table : tables) {
			final Element parentEntry = table.get(parentId);
			final LMUnit cloneUnit = new LMUnit(cloneId, table);
			for (final String key : parentEntry.keySet()) {
				cloneUnit.setField(key, parentEntry.getField(key));
			}
			table.put(cloneId, cloneUnit);
		}
		units.put(new StringKey(cloneId), new WarcraftObject(cloneId, this));
	}
}
