package com.hiveworkshop.rms.parsers.slk;

import java.util.*;

public class WarcraftData extends ObjectData {
	List<DataTable> tables = new ArrayList<>();
	Map<StringKey, DataTable> tableMap = new HashMap<>();
	Map<StringKey, WarcraftObject> units = new HashMap<>();

	public WarcraftData() {
	}

	public WarcraftData add(final DataTable data, final String name, final boolean canMake) {
		tableMap.put(new StringKey(name), data);
		tables.add(data);
		if (canMake) {
			for (String id : data.keySet()) {
				if (!units.containsKey(new StringKey(id))) {
					units.put(new StringKey(id), new WarcraftObject(data.get(id).getId(), this));
				}
			}
		}
		return this;
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
		Set<String> keySet = new HashSet<>();
		for (StringKey key : units.keySet()) {
			keySet.add(key.getString());
		}
		return keySet;
	}

	public void cloneUnit(final String parentId, final String cloneId) {
		for (DataTable table : tables) {
			Element parentEntry = table.get(parentId);
			LMUnit cloneUnit = new LMUnit(cloneId, table);
			for (String key : parentEntry.keySet()) {
				cloneUnit.setField(key, parentEntry.getField(key));
			}
			table.put(cloneId, cloneUnit);
		}
		units.put(new StringKey(cloneId), new WarcraftObject(cloneId, this));
	}
}
