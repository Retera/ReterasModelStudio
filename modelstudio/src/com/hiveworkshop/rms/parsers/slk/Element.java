package com.hiveworkshop.rms.parsers.slk;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class Element extends GameObject {
	HashMap<StringKey, List<String>> fields;
	ObjectData parentTable;
	HashMap<String, List<Element>> hashedLists = new HashMap<>();

	public Element(String id, DataTable parentTable) {
		this.id = id;
		this.parentTable = parentTable;
		fields = new HashMap<>();
		placeholderTexPath = "ReplaceableTextures\\CommandButtons\\BTNTemp.blp";
	}


	@Override
	public void setField(final String field, final String value) {
		setField(field, value, 0);
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
	public String getField(final String field) {
		List<String> list = fields.get(new StringKey(field));
		if (list != null) {
			return String.join(",", list);
		}
		return "";
	}

	@Override
	public String getField(final String field, final int index) {
		List<String> list = fields.get(new StringKey(field));
		if (list != null && index < list.size()) {
			return list.get(index);
		}
		return "";
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

		List<String> list = fields.get(new StringKey(field));
		if(list != null){
			for (String buildingId : list) {
				GameObject referencedUnit = parentTable.get(buildingId);
				if (referencedUnit != null) {
					fieldAsList.add(referencedUnit);
				}
			}
		}
		return fieldAsList;
	}

	@Override
	public String toString() {
		return getField("Name");
	}


	public void addToList(final String parentId, final String fieldId) {
		String parentField = getField(fieldId);
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField(fieldId, parentField);
		}
	}

	@Override
	public ObjectData getTable() {
		return parentTable;
	}


	public List<GameObject> requires() {
		List<GameObject> requirements = getFieldAsList("Requires", parentTable);
		List<Integer> reqLvls = requiresLevels();
		// parentTable.updateListWithLevels(requirements, reqLvls);
		return requirements;
	}

	public List<Integer> requiresLevels() {
		String stringList = getField("Requiresamount");
		String[] listAsArray = stringList.split(",");
		LinkedList<Integer> output = new LinkedList<>();
		if (listAsArray.length > 0 && !listAsArray[0].equals("")) {
			for (String levelString : listAsArray) {
				Integer level = Integer.parseInt(levelString);
				if (level != null) {
					output.add(level);
				}
			}
		}
		return output;
	}

	public List<GameObject> builds() {
		return getFieldAsList("Builds", parentTable);
	}
	public List<GameObject> parents() {
		return getFieldAsList("Parents", parentTable);
	}

	public List<GameObject> children() {
		return getFieldAsList("Children", parentTable);
	}

	public List<GameObject> requiredBy() {
		return getFieldAsList("RequiredBy", parentTable);
	}

	public List<GameObject> trains() {
		return getFieldAsList("Trains", parentTable);
	}

	public List<GameObject> upgrades() {
		return getFieldAsList("Upgrade", parentTable);
	}

	public List<GameObject> researches() {
		return getFieldAsList("Researches", parentTable);
	}

	public List<GameObject> dependencyOr() {
		return getFieldAsList("DependencyOr", parentTable);
	}

	public List<GameObject> abilities() {
		return getFieldAsList("abilList", parentTable);
	}

	public int getTechTier() {
		String tier = getField("Custom Field: TechTier");
		if (tier == null) {
			return -1;
		}
		return Integer.parseInt(tier);
	}

	public void setTechTier(int i) {
		setField("Custom Field: TechTier", i + "");
	}

	public int getTechDepth() {
		String tier = getField("Custom Field: TechDepth");
		if (tier == null) {
			return -1;
		}
		return Integer.parseInt(tier);
	}

	public void setTechDepth(int i) {
		setField("Custom Field: TechDepth", i + "");
	}

	public void addParent(String parentId) {
		addField(parentId, "Parents");
	}

	public void addChild(String parentId) {
		addField(parentId, "Children");
	}

	public void addRequiredBy(String parentId) {
		addField(parentId, "RequiredBy");
	}

	public void addResearches(String parentId) {
		addField(parentId, "Researches");
	}

	private void addField(String parentId, String fieldName) {
		String parentField = getField(fieldName);
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField(fieldName, parentField);
		}
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
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
