package com.hiveworkshop.wc3.units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.resources.WEString;

public abstract class HashedGameObject implements GameObject {
	HashMap<String, String> fields = new HashMap<>();
	String id;
	ObjectData parentTable;

	transient HashMap<String, List<GameObject>> hashedLists = new HashMap<>();

	public HashedGameObject(final String id, final ObjectData table) {
		this.id = id;
		parentTable = table;
	}

	@Override
	public void setField(final String field, final String value) {
		fields.put(field, value);
	}

	@Override
	public String getField(final String field) {
		String value = "";
		if (fields.get(field) != null) {
			value = fields.get(field);
		}
		return value;
	}

	public boolean hasField(final String field) {
		return fields.containsKey(field);
	}

	@Override
	public int getFieldValue(final String field) {
		int i = 0;
		try {
			i = Integer.parseInt(getField(field));
		} catch (final NumberFormatException e) {

		}
		return i;
	}

	@Override
	public List<GameObject> getFieldAsList(final String field, final ObjectData parentTable) {
		List<GameObject> fieldAsList;// = hashedLists.get(field);
		// if( fieldAsList == null ) {
		fieldAsList = new ArrayList<>();
		final String stringList = getField(field);
		final String[] listAsArray = stringList.split(",");
		if (listAsArray != null && listAsArray.length > 0) {
			for (final String buildingId : listAsArray) {
				final GameObject referencedUnit = parentTable.get(buildingId);
				if (referencedUnit != null) {
					fieldAsList.add(referencedUnit);
				}
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

	@Override
	public String getName() {
		String name = getField("Name");
		boolean nameKnown = name.length() >= 1;
		if (!nameKnown && !getField("code").equals(id) && getField("code").length() >= 4) {
			final GameObject other = parentTable.get(getField("code").substring(0, 4));
			if (other != null) {
				name = other.getName();
				nameKnown = true;
			}
		}
		if (!nameKnown && getField("EditorName").length() > 1) {
			name = getField("EditorName");
			nameKnown = true;
		}
		if (!nameKnown && getField("Editorname").length() > 1) {
			name = getField("Editorname");
			nameKnown = true;
		}
		if (!nameKnown && getField("BuffTip").length() > 1) {
			name = getField("BuffTip");
			nameKnown = true;
		}
		if (!nameKnown && getField("Bufftip").length() > 1) {
			name = getField("Bufftip");
			nameKnown = true;
		}
		if (nameKnown && name.startsWith("WESTRING")) {
			if (!name.contains(" ")) {
				name = WEString.getString(name);
			} else {
				final String[] names = name.split(" ");
				name = "";
				for (final String subName : names) {
					if (name.length() > 0) {
						name += " ";
					}
					if (subName.startsWith("WESTRING")) {
						name += WEString.getString(subName);
					} else {
						name += subName;
					}
				}
			}
			if (name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1);
			}
			setField("Name", name);
		}
		if (!nameKnown) {
			name = WEString.getString("WESTRING_UNKNOWN") + " '" + getId() + "'";
		}
		if (getField("campaign").startsWith("1") && Character.isUpperCase(getId().charAt(0))) {
			name = getField("Propernames");
			if (name.contains(",")) {
				name = name.split(",")[0];
			}
		}
		String suf = getField("EditorSuffix");
		if (suf.length() > 0 && !suf.equals("_")) {
			if (suf.startsWith("WESTRING")) {
				suf = WEString.getString(suf);
			}
			if (!suf.startsWith(" ")) {
				name += " ";
			}
			name += suf;
		}
		return name;
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
		return fields.keySet();
	}
}
