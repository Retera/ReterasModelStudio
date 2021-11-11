package com.hiveworkshop.rms.parsers.slk;

import java.util.*;

public abstract class HashedGameObject extends GameObject {
	HashMap<StringKey, List<String>> fields = new HashMap<>();
	String id;
	ObjectData parentTable;

	transient HashMap<String, List<GameObject>> hashedLists = new HashMap<>();

	public HashedGameObject(final String id, final ObjectData table) {
		this.id = id;
		parentTable = table;
	}

	@Override
	public void setField(final String field, final String value) {
		final StringKey key = new StringKey(field);
		List<String> list = fields.get(key);
		if (list == null) {
			list = new ArrayList<>();
			fields.put(key, list);
			list.add(value);
		} else {
			list.set(0, value);
		}
	}

	@Override
	public String getField(final String field) {
		final String value = "";
		if (fields.get(new StringKey(field)) != null) {
			final List<String> list = fields.get(new StringKey(field));
			final StringBuilder sb = new StringBuilder();
			if (list != null) {
				for (final String str : list) {
					if (sb.length() != 0) {
						sb.append(',');
					}
					sb.append(str);
				}
				return sb.toString();
//				value = list.get(0);
			}
		}
		return value;
	}

	public boolean hasField(final String field) {
		return fields.containsKey(new StringKey(field));
	}

	@Override
	public int getFieldValue(final String field) {
		try {
			return Integer.parseInt(getField(field));
		} catch (final NumberFormatException e) {

		}
		return 0;
	}

	@Override
	public void setField(final String field, final String value, final int index) {
		final StringKey key = new StringKey(field);
		List<String> list = fields.get(key);
		if (list == null) {
			if (index == 0) {
				list = new ArrayList<>();
				fields.put(key, list);
				list.add(value);
			} else {
				throw new IndexOutOfBoundsException();
			}
		} else {
			if (list.size() == index) {
				list.add(value);
			} else {
				list.set(index, value);
			}
		}
	}

	@Override
	public String getField(final String field, final int index) {
		String value = "";
		if (fields.get(new StringKey(field)) != null) {
			final List<String> list = fields.get(new StringKey(field));
			if (list != null) {
				if (list.size() > index) {
					value = list.get(index);
				}
			}
		}
		return value;
	}

	@Override
	public int getFieldValue(final String field, final int index) {
		try {
			return Integer.parseInt(getField(field, index));
		} catch (final NumberFormatException e) {

		}
		return 0;
	}

	@Override
	public List<GameObject> getFieldAsList(String field, ObjectData parentTable) {
		List<GameObject> fieldAsList = new ArrayList<>();

		String[] listAsArray = getField(field).split(",");
		for (String buildingId : listAsArray) {
			GameObject referencedUnit = parentTable.get(buildingId);
			if (referencedUnit != null) {
				fieldAsList.add(referencedUnit);
			}
		}
		// hashedLists.put(field, fieldAsList);
		// }
		return fieldAsList;
	}

	@Override
	public String toString() {
		return getField("Name");
	}

	@Override
	public String getId() {
		return id;
	}

	public void addToList(final String parentId, final String list) {
		String parentField = getField(list);
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField(list, parentField);
		}
	}

	@Override
	public ObjectData getTable() {
		return parentTable;
	}

	@Override
	public Set<String> keySet() {
		final Set<String> keySet = new HashSet<>();
		for (final StringKey key : fields.keySet()) {
			keySet.add(key.getString());
		}
		return keySet;
	}
}
